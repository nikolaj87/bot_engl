package com.telegrambot.service;

import com.telegrambot.cache.Cache;
import com.telegrambot.cache.CacheList;
import com.telegrambot.entity.Homework;
import com.telegrambot.entity.Student;
import com.telegrambot.entity.Word;
import com.telegrambot.utils.KeyboardGenerator;
import com.telegrambot.utils.MessageGenerator;
import com.telegrambot.repository.HomeTaskRepository;
import com.telegrambot.repository.StudentRepository;
import com.telegrambot.repository.WordRepository;


import org.hibernate.type.descriptor.sql.internal.Scale6IntervalSecondDdlType;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@org.springframework.stereotype.Service
public class ServiceImpl implements Service {

    private final Cache cache;
    private final CacheList cacheList;
    private final MessageGenerator generator;
    private final KeyboardGenerator keyGenerator;
    private final StudentRepository studentRepository;
    private final WordRepository wordRepository;
    private final HomeTaskRepository homeTaskRepository;

    @Value("${app_version}")
    private String app_version;

    @Value("${admin_id}")
    private long currentStudentId;

    @Value("${admin_id}")
    private long adminId;

    public ServiceImpl(Cache cache, CacheList cacheList, MessageGenerator generator, KeyboardGenerator keyGenerator, StudentRepository studentRepository, WordRepository wordRepository, HomeTaskRepository homeTaskRepository) {
        this.cache = cache;
        this.cacheList = cacheList;
        this.generator = generator;
        this.keyGenerator = keyGenerator;
        this.studentRepository = studentRepository;
        this.wordRepository = wordRepository;
        this.homeTaskRepository = homeTaskRepository;
    }

    public long getStudentId() {
        return currentStudentId;
    }

    @Override
    public List<SendMessage> addHomeTask(long chatId, String messageText) {
        if (currentStudentId == adminId) {
            return List.of(new SendMessage(String.valueOf(adminId), "себе нельзя сохранять домашку, выбери студента"));
        }
        String homeworkText = messageText.substring(4);
        Optional<Homework> homeworkOptional = homeTaskRepository.findHomeTaskById(currentStudentId);
        if (homeworkOptional.isPresent()) {
            Homework homework = homeworkOptional.get();
            homework.setActive(1);
            homework.setDescription(homeworkText);
        } else {
            Homework homework = new Homework(currentStudentId, homeworkText, 1);
            homeTaskRepository.save(homework);
        }
        SendMessage adminMessage = new SendMessage(String.valueOf(currentStudentId), generator.homeworkMessage() + homeworkText);
        SendMessage studentMessage = new SendMessage(String.valueOf(chatId), "домашка сохранена: " + homeworkText);
        return List.of(adminMessage, studentMessage);
    }

    @Override
    public List<SendMessage> handleHomeworkReply(long studentId, String reply) {
        if (reply.equals("hwYes")) {
            homeTaskRepository.updateHomeTaskByIdSetIsActiveFalse(studentId);
            return List.of(new SendMessage(String.valueOf(studentId), generator.correctMessage()));
        } else {
            return List.of(new SendMessage(String.valueOf(studentId), generator.remindTomorrowHomework()));
        }
    }

    @Override
    public List<SendMessage> homeWorkRemind() {
        List<SendMessage> messages = new LinkedList<>();
        List<Student> allStudents = studentRepository.findAll();
        allStudents = allStudents.stream()
                .filter(student -> student.getId() != currentStudentId).toList();
        for (Student student : allStudents) {
            Optional<Homework> homeworkOptional = homeTaskRepository.findHomeTaskById(student.getId());
            if (homeworkOptional.isPresent()) {
                if (homeworkOptional.get().isActive() == 1) {
                    String description = homeworkOptional.get().getDescription();
                    var message = new SendMessage(String.valueOf(student.getId()), generator.getHomeworkRemind() + description);
                    message.setReplyMarkup(keyGenerator.getYesNoBoard());
                    messages.add(message);
                }
            }
        }
        if (messages.isEmpty()) {
            messages.add(new SendMessage(String.valueOf(adminId), "бот хотел раздать домашки, но они все поделаны :("));
        }
        return messages;
    }

    @Override
    public List<SendMessage> createTaskList() {
        List<SendMessage> messages = new LinkedList<>();

        List<Student> allStudents = studentRepository.findAll();
        allStudents.stream()
                .filter(student -> student.getId() != currentStudentId && student.getId() != adminId)
                .forEach(student -> {
                    Optional<Word> anyWordOptional = wordRepository.getAnyWordByStudentId(student.getId());
                    if (anyWordOptional.isPresent()) {
                        Word anyWord = anyWordOptional.get();
                        //c помошью кеша я буду отличать какие слова там лежат
                        //если это слово имеет приставку 4 то это слова из @scheduler
                        cache.put(student.getId(), "4" + anyWord.getWordEnglish());
                        messages.add(new SendMessage(String.valueOf(student.getId()), generator.askMessage() + anyWord.getWordOriginal()));
                    }
                });
        return messages;
    }

    @Override
    public List<SendMessage> handleStudentMessage(long studentId, String messageText) {
        //если учитель выбрал данного студента то идет урок и учитель получает сообщения студента
        //также учитель сам себе не отправляет свои сообщения
        if (studentId == currentStudentId && adminId != currentStudentId) {
            return List.of(new SendMessage(String.valueOf(adminId), messageText));
        }
        //если в кеше нет вопроса к студенту - пускай ждет урока. Даем wait message
        if (!cache.cacheCheck(studentId)) {
            return List.of(new SendMessage(String.valueOf(studentId), generator.waitMessage()));
        }
        //если есть в кеше - идет логика проверки ответа
        String englishWordCache = cache.get(studentId);
        cache.remove(studentId);
        //сразу проверю ответ и добавлю сообщение после проверки - правильно или не правильный ответ
        List<SendMessage> messagesForStudent = new ArrayList<>();


        if (englishWordCache.substring(1).equals(messageText.trim().toLowerCase())) {
            messagesForStudent.add(new SendMessage(String.valueOf(studentId), "✅ " + generator.correctMessage()));
        } else {
            messagesForStudent.add(new SendMessage(String.valueOf(studentId), "❌ " + generator.wrongMessage() + " Correct answer \n" + "✅ " + englishWordCache.substring(1)));
        }
        //в данном случай я задавал вопрос и кеш пуст и больше сообщений не будет
        if (englishWordCache.startsWith("4")) {
            return messagesForStudent;
        }

        if (cacheList.isEmpty(studentId)) {
            //наполняем лист
            if (englishWordCache.startsWith("1")) {
                List<SendMessage> messages = studyNewButton(studentId, messageText);
                messagesForStudent.add(messages.get(0));
                messagesForStudent.add(messages.get(1));
                return messagesForStudent;
            }
            if (englishWordCache.startsWith("2")) {
                List<SendMessage> messages = studyAllButton(studentId, messageText);
                messagesForStudent.add(messages.get(0));
                messagesForStudent.add(messages.get(1));
                return messagesForStudent;
            }
            if (englishWordCache.startsWith("3")) {
                List<SendMessage> messages = studyArchiveButton(studentId, messageText);
                messagesForStudent.add(messages.get(0));
                messagesForStudent.add(messages.get(1));
                return messagesForStudent;
            }
            if (englishWordCache.startsWith("5")) {
                SendMessage sendMessage = new SendMessage(String.valueOf(studentId), "end of list");
                messagesForStudent.add(sendMessage);
                return messagesForStudent;
            }
            if (englishWordCache.startsWith("6")) {
                SendMessage sendMessage = new SendMessage(String.valueOf(studentId), "end of list");
                messagesForStudent.add(sendMessage);
                return messagesForStudent;
            }

        }
        //иначе кеш не пуст и я могу взять слово из кеш
        Word anyWord = cacheList.getAndDelete(studentId);
        if (englishWordCache.startsWith("1")) {
            cache.put(studentId, "1" + anyWord.getWordEnglish());
            SendMessage sendMessage = new SendMessage(String.valueOf(studentId), generator.askMessage() + anyWord.getWordOriginal());
            messagesForStudent.add(sendMessage);
            return messagesForStudent;
        }
        if (englishWordCache.startsWith("2")) {
            cache.put(studentId, "2" + anyWord.getWordEnglish());
            SendMessage sendMessage = new SendMessage(String.valueOf(studentId), generator.askMessage() + anyWord.getWordOriginal());
            sendMessage.setReplyMarkup(keyGenerator.getAllWordButtons(anyWord.getWordEnglish()));
            messagesForStudent.add(sendMessage);
            return messagesForStudent;
        }
        if (englishWordCache.startsWith("3")) {
            cache.put(studentId, "3" + anyWord.getWordEnglish());
            SendMessage sendMessage = new SendMessage(String.valueOf(studentId), generator.askMessage() + anyWord.getWordOriginal());
            sendMessage.setReplyMarkup(keyGenerator.getArchiveWordButtons(anyWord.getWordEnglish()));
            messagesForStudent.add(sendMessage);
            return messagesForStudent;
        }
        if (englishWordCache.startsWith("5")) {
            cache.put(studentId, "5" + anyWord.getWordEnglish());
            SendMessage sendMessage = new SendMessage(String.valueOf(studentId), generator.askMessage() + anyWord.getWordOriginal());
            messagesForStudent.add(sendMessage);
            return messagesForStudent;
        }
        if (englishWordCache.startsWith("6")) {
            cache.put(studentId, "6" + anyWord.getGroupName());
            SendMessage sendMessage = new SendMessage(String.valueOf(studentId), anyWord.getWordEnglish().substring(anyWord.getWordEnglish().indexOf(" ")));
            sendMessage.setReplyMarkup(keyGenerator.getDoMakeButtons());
            messagesForStudent.add(sendMessage);
            return messagesForStudent;
        }
        return messagesForStudent;
    }

    @Override
    public List<SendMessage> studyNewButton(long chatId, String messageText) {
        if (chatId == currentStudentId && adminId != currentStudentId) {
            return List.of(new SendMessage(String.valueOf(chatId), generator.laterMessage()));
        }
        List<Word> allNewWords = wordRepository.getAllNewWords(chatId);
        if (!allNewWords.isEmpty()) {
            Word anyWordFromList = cacheList.putAndReturnAny(chatId, allNewWords);
            //c помошью кеша я буду отличать какие слова там лежат
            //если это слово имеет приставку 1 то это новые слова
            cache.put(chatId, "1" + anyWordFromList.getWordEnglish());
            SendMessage number = new SendMessage(String.valueOf(chatId), allNewWords.size() + " words found");
            SendMessage someWord = new SendMessage(String.valueOf(chatId), generator.askMessage() + anyWordFromList.getWordOriginal());
            return List.of(number, someWord);
        }
        return List.of(new SendMessage(String.valueOf(chatId), "you haven`t new words :("));
    }

    @Override
    public List<SendMessage> studyAllButton(long chatId, String messageText) {
        if (chatId == currentStudentId && adminId != currentStudentId) {
            return List.of(new SendMessage(String.valueOf(chatId), generator.laterMessage()));
        }
        List<Word> allWords = wordRepository.getAllStudentWords(chatId);
        if (!allWords.isEmpty()) {
            Word anyWord = cacheList.putAndReturnAny(chatId, allWords);
            //c помошью кеша я буду отличать какие слова там лежат
            //если это слово имеет приставку 2 то это все слова
            cache.put(chatId, "2" + anyWord.getWordEnglish());
            SendMessage number = new SendMessage(String.valueOf(chatId), allWords.size() + " words found");
            SendMessage sendMessage = new SendMessage(String.valueOf(chatId), generator.askMessage() + anyWord.getWordOriginal());
            sendMessage.setReplyMarkup(keyGenerator.getAllWordButtons(anyWord.getWordEnglish()));
            return List.of(number, sendMessage);
        }
        return List.of(new SendMessage(String.valueOf(chatId), "you have 0 words :("));
    }

    @Override
    public List<SendMessage> studyArchiveButton(long chatId, String messageText) {
        if (chatId == currentStudentId && adminId != currentStudentId) {
            return List.of(new SendMessage(String.valueOf(chatId), generator.laterMessage()));
        }
        List<Word> archiveWords = wordRepository.getArchiveStudentWords(chatId);
        if (!archiveWords.isEmpty()) {
            Word anyWord = cacheList.putAndReturnAny(chatId, archiveWords);
            //c помошью кеша я буду отличать какие слова там лежат
            //если это слово имеет приставку 3 то это слова из архива
            cache.put(chatId, "3" + anyWord.getWordEnglish());
            SendMessage number = new SendMessage(String.valueOf(chatId), archiveWords.size() + " words found");
            SendMessage sendMessage = new SendMessage(String.valueOf(chatId), generator.askMessage() + anyWord.getWordOriginal());
            sendMessage.setReplyMarkup(keyGenerator.getArchiveWordButtons(anyWord.getWordEnglish()));
            return List.of(number, sendMessage);
        }
        return List.of(new SendMessage(String.valueOf(chatId), "you haven`t words in archive :("));
    }

    @Override
    public List<SendMessage> studyCollocationsButton(long chatId) {
        if (chatId == currentStudentId && adminId != currentStudentId) {
            return List.of(new SendMessage(String.valueOf(chatId), generator.laterMessage()));
        }
        //тут достаю слова из категории collocations
        List<Word> collocations = wordRepository.getCollocationsWords(); //!!!!!!!!!!!!!!!!!!!!!!!!!!!
        if (!collocations.isEmpty()) {
            Word anyWord = cacheList.putAndReturnAny(chatId, collocations);
            //c помошью кеша я буду отличать какие слова там лежат
            //если это слово имеет приставку 4 то это слова collocations
            cache.put(chatId, "5" + anyWord.getWordEnglish());
            SendMessage number = new SendMessage(String.valueOf(chatId), collocations.size() + " words found");
            SendMessage sendMessage = new SendMessage(String.valueOf(chatId), generator.askMessage() + anyWord.getWordOriginal());
            return List.of(number, sendMessage);
        }
        return List.of(new SendMessage(String.valueOf(chatId), "no words found"));
    }

    @Override
    public List<SendMessage> studyDoMakeButton(long chatId) {
        if (chatId == currentStudentId && adminId != currentStudentId) {
            return List.of(new SendMessage(String.valueOf(chatId), generator.laterMessage()));
        }
        //тут достаю слова из категории do make
        List<Word> doMake = wordRepository.getDoMakeWords(); //!!!!!!!!!!!!!!!!!!!!!!!!!!!
        if (!doMake.isEmpty()) {
            Word anyWord = cacheList.putAndReturnAny(chatId, doMake);
            //c помошью кеша я буду отличать какие слова там лежат
            //если это слово имеет приставку 6 то это слова doMake
            cache.put(chatId, "6" + anyWord.getGroupName());
            SendMessage number = new SendMessage(String.valueOf(chatId), doMake.size() + " words found");
            SendMessage task = new SendMessage(String.valueOf(chatId), "choose DO or MAKE");
            SendMessage sendMessage = new SendMessage(String.valueOf(chatId), anyWord.getWordEnglish().substring(anyWord.getWordEnglish().indexOf(" ")));
            sendMessage.setReplyMarkup(keyGenerator.getDoMakeButtons());
            return List.of(number, task, sendMessage);
        }
        return List.of(new SendMessage(String.valueOf(chatId), "no words found"));

    }

    @Override
    public List<SendMessage> wordToArchive(long studentId, String word) {
//        String wordToArchive = cache.get(studentId).substring(1);
        wordRepository.wordToArchive(studentId, word);
        return List.of(new SendMessage(String.valueOf(studentId), "\"" + word + "\" moved to archive"));
    }

    @Override
    public List<SendMessage> wordToList(long studentId, String word) {
//        String wordToArchive = cache.get(studentId).substring(1);
        wordRepository.wordToList(studentId, word);
        return List.of(new SendMessage(String.valueOf(studentId), "\"" + word + "\" moved to list"));
    }

    @Override
    public List<SendMessage> wordListen(long studentId) {
        return List.of(new SendMessage(String.valueOf(studentId), "soon :)"));
    }

    @Override
    public List<SendMessage> deleteById(String request) {
        String wordIdString = request.replaceAll("\\D", "");
        long wordId = Long.parseLong(wordIdString);
        Optional<Word> word = wordRepository.findById(wordId);
        if (word.isEmpty()) {
            return List.of(new SendMessage(String.valueOf(adminId), "нет такого Id - " + wordId));
        } else {
            wordRepository.deleteById(wordId);
            SendMessage message = new SendMessage(String.valueOf(adminId), "слово " + word.get().getWordEnglish() +
                    " удалено");
            return List.of(message);
        }
    }

    @Override
    public List<SendMessage> getLastWords() {
        List<Word> lastWords = wordRepository.getLast30Words(currentStudentId);
        if (lastWords.isEmpty()) {
            return List.of(new SendMessage(String.valueOf(adminId), "found 0 words"));
        }
        StringBuilder reply = new StringBuilder();
        for (Word word : lastWords) {
            reply.append(word.getId());
            reply.append(". ");
            reply.append(word.getWordEnglish());
            reply.append(" - ");
            reply.append(word.getWordOriginal());
            reply.append("\n");
        }
        return List.of(new SendMessage(String.valueOf(adminId), reply.toString()));
    }

    @Override
    public List<SendMessage> clearCache(long chatId) {
        cache.remove(chatId);
        cacheList.remove(chatId);
        return List.of(new SendMessage(String.valueOf(chatId), generator.waitMessage()));
    }

    @Override
    public List<SendMessage> addWord(String text, long currentId) {
        String wordEnglish = text.substring(1, text.lastIndexOf("+")).trim().toLowerCase();
        String wordOrigin = text.substring(text.lastIndexOf("+") + 1).trim().toLowerCase();
        Word word = new Word(0L, wordEnglish, wordOrigin, currentId, new Timestamp(System.currentTimeMillis()), 0);
        List<SendMessage> messages = new ArrayList<>();
        if (currentId == adminId) {
            messages.add(new SendMessage(String.valueOf(currentStudentId), wordOrigin + " - a new word to learn"));
            messages.add(new SendMessage(String.valueOf(adminId), "новое слово сохранено: " + wordEnglish + " = " + wordOrigin));
            word.setStudentId(currentStudentId);
        } else {
            messages.add(new SendMessage(String.valueOf(currentId), "a new word saved:\n" + wordEnglish + " = " + wordOrigin));
        }
        wordRepository.save(word);
        return messages;
    }

    @Override
    public List<SendMessage> initializeNewStudent(Update update, long currentId) {
        Student student = new Student(update.getMessage().getChatId(), update.getMessage().getFrom().getFirstName(),
                new Timestamp(System.currentTimeMillis()));
        List<SendMessage> messages = new ArrayList<>();
        if (studentRepository.findById(currentId).isEmpty()) {
            studentRepository.save(student);
            SendMessage adminMessage = new SendMessage(String.valueOf(adminId), "студент запустил english_bot! id = " +
                    student.getId() + " имя = " + student.getName());
            messages.add(adminMessage);
        }
        SendMessage versionMessage = new SendMessage(String.valueOf(currentId), "welcome to alexandra_english_bot!:) \n" + app_version);
        SendMessage rusMessage = new SendMessage(String.valueOf(currentId), generator.rusMessage());
        SendMessage ukrMessage = new SendMessage(String.valueOf(currentId), generator.ukrMessage());
        SendMessage polMessage = new SendMessage(String.valueOf(currentId), generator.polMessage());
        messages.add(versionMessage);
        messages.add(rusMessage);
        messages.add(ukrMessage);
        messages.add(polMessage);
        if (currentId == adminId) {
            messages.add(new SendMessage(String.valueOf(adminId), generator.commandsMessage()));
        }
        return messages;
    }

    @Override
    public List<SendMessage> getAllStudents(Update update) {
        List<Student> allStudents = studentRepository.findAll();
        return keyGenerator.generateStudentList(allStudents, adminId);
    }

    @Override
    public List<SendMessage> switchStudent(String text) {
        String studentId = text.substring(7, text.indexOf(" "));
        String studentName = text.substring(text.indexOf(" ")).toUpperCase();
        SendMessage previousStudentMessage = new SendMessage(String.valueOf(currentStudentId), generator.teacherLeftChat());
        currentStudentId = Long.parseLong(studentId);
        SendMessage adminMessage = new SendMessage(String.valueOf(adminId), "переключено на студента" + studentName +
                "\nТеперь он получает прямые сообщения и можно сохранить для него слова и домашку");
        SendMessage newStudentMessage = new SendMessage(String.valueOf(currentStudentId), generator.teacherEntersChat());
//        System.out.println(currentStudentId);
        return List.of(adminMessage, previousStudentMessage, newStudentMessage);
    }
}
