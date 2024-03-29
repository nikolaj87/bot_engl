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


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ServiceImpl implements Service {

    private final Cache cache;
    private final CacheList cacheList;
    private final MessageGenerator generator;
    private final KeyboardGenerator keyboards;
    private final StudentRepository studentRepository;
    private final WordRepository wordRepository;
    private final HomeTaskRepository homeTaskRepository;

    @Value("${app_version}")
    private String app_version;

    @Value("${admin_id}")
    private long currentStudentId;

    @Value("${admin_id}")
    private long adminId;

    @Value("${words_on_page}")
    private int wordsOnPage;

    @PersistenceContext
    private EntityManager entityManager;

    public ServiceImpl(Cache cache, CacheList cacheList, MessageGenerator generator, KeyboardGenerator keyboards, StudentRepository studentRepository, WordRepository wordRepository, HomeTaskRepository homeTaskRepository) {
        this.cache = cache;
        this.cacheList = cacheList;
        this.generator = generator;
        this.keyboards = keyboards;
        this.studentRepository = studentRepository;
        this.wordRepository = wordRepository;
        this.homeTaskRepository = homeTaskRepository;
    }

    public long getCurrentStudentId() {
        return currentStudentId;
    }

    public void setCurrentStudentId(long currentStudentId) {
        this.currentStudentId = currentStudentId;
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
                    message.setReplyMarkup(keyboards.getYesNoBoard());
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
                    Optional<Word> anyWordOptional = wordRepository.getAnyNewWordByStudentId(student.getId(), wordsOnPage);
                    if (anyWordOptional.isPresent()) {
                        Word anyWord = anyWordOptional.get();
                        entityManager.detach(anyWord);
                        anyWord.setGroupName("bot");
                        cache.put(student.getId(), anyWord);
                        messages.add(new SendMessage(String.valueOf(student.getId()), generator.askMessage() + anyWord.getWordOriginal()));
                    }
                });
        return messages;
    }

    @Override
    public List<SendMessage> studyNewButton(long chatId, String messageText) {
        List<Word> allNewWords = wordRepository.getAllNewWords(chatId);
        if (!allNewWords.isEmpty()) {
            allNewWords.forEach(word -> entityManager.detach(word));
            allNewWords.forEach(word -> word.setGroupName("new"));
            SendMessage number = new SendMessage(String.valueOf(chatId), allNewWords.size() + " words found");
            Word anyWordFromList = cacheList.putAndReturnAny(chatId, allNewWords);
            cache.put(chatId, anyWordFromList);
            SendMessage someWord = new SendMessage(String.valueOf(chatId), generator.askMessage() + anyWordFromList.getWordOriginal());
            return List.of(number, someWord);
        }
        return List.of(new SendMessage(String.valueOf(chatId), "you haven`t new words :("));
    }

    @Override
    public List<SendMessage> studyAllButton(long chatId, String messageText) {
        List<Word> allWords = wordRepository.getAllStudentWords(chatId);
        if (!allWords.isEmpty()) {
            allWords.forEach(word -> entityManager.detach(word));
            allWords.forEach(word -> word.setGroupName("all"));
            SendMessage number = new SendMessage(String.valueOf(chatId), allWords.size() + " words found");
            Word anyWord = cacheList.putAndReturnAny(chatId, allWords);
            cache.put(chatId, anyWord);
            SendMessage sendMessage = new SendMessage(String.valueOf(chatId), generator.askMessage() + anyWord.getWordOriginal());
            sendMessage.setReplyMarkup(keyboards.getAllWordButtons(anyWord.getWordEnglish()));
            return List.of(number, sendMessage);
        }
        return List.of(new SendMessage(String.valueOf(chatId), "you have 0 words :("));
    }

    @Override
    public List<SendMessage> studyArchiveButton(long chatId, String messageText) {
        List<Word> archiveWords = wordRepository.getArchiveStudentWords(chatId);
        if (!archiveWords.isEmpty()) {
            archiveWords.forEach(word -> entityManager.detach(word));
            archiveWords.forEach(word -> word.setGroupName("archive"));
            SendMessage number = new SendMessage(String.valueOf(chatId), archiveWords.size() + " words found");
            Word anyWord = cacheList.putAndReturnAny(chatId, archiveWords);
            cache.put(chatId, anyWord);
            SendMessage sendMessage = new SendMessage(String.valueOf(chatId), generator.askMessage() + anyWord.getWordOriginal());
            sendMessage.setReplyMarkup(keyboards.getArchiveWordButtons(anyWord.getWordEnglish()));
            return List.of(number, sendMessage);
        }
        return List.of(new SendMessage(String.valueOf(chatId), "you haven`t words in archive :("));
    }

    @Override
    public List<SendMessage> studyCollocationsButton(long chatId) {
        int collocationWordsNumber = wordRepository.getCollocationsWordNumber();
        if (collocationWordsNumber == 0) {
            return List.of(new SendMessage(String.valueOf(chatId), "no words found"));
        }
        int pagesNumber = collocationWordsNumber / wordsOnPage;
        if (collocationWordsNumber % wordsOnPage != 0) {
            pagesNumber++;
        }
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), "choose collocations page");
        sendMessage.setReplyMarkup(keyboards.addPageNumberButtons(pagesNumber));
        return List.of(sendMessage);
    }

    @Override
    public List<SendMessage> studyCollocationsButtonPage(long chatId, String data) {
        int page = Integer.parseInt(data.substring(12));
        int wordsToSkip = (page - 1) * wordsOnPage;
        List<Word> collocations = wordRepository.getCollocationsWordsPage(wordsToSkip, wordsOnPage);
        SendMessage number = new SendMessage(String.valueOf(chatId), collocations.size() + " words found");
        Word anyWord = cacheList.putAndReturnAny(chatId, collocations);
        cache.put(chatId, anyWord);
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), generator.askMessage() + anyWord.getWordOriginal());
        return List.of(number, sendMessage);
    }

    @Override
    public List<SendMessage> studyDoMakeButton(long chatId) {
        List<Word> doMake = wordRepository.getDoMakeWords();
        if (!doMake.isEmpty()) {
            SendMessage number = new SendMessage(String.valueOf(chatId), doMake.size() + " words found");
            Word anyWord = cacheList.putAndReturnAny(chatId, doMake);
            cache.put(chatId, anyWord);
            SendMessage task = new SendMessage(String.valueOf(chatId), "choose DO or MAKE");
            SendMessage sendMessage = new SendMessage(String.valueOf(chatId), anyWord.getWordOriginal().substring(anyWord.getWordOriginal().indexOf(" ")));
            sendMessage.setReplyMarkup(keyboards.getDoMakeButtons());
            return List.of(number, task, sendMessage);
        }
        return List.of(new SendMessage(String.valueOf(chatId), "no words found"));

    }

    @Override
    public EditMessageText wordToArchive(String data, Message message) {
        String wordToArchive = data.substring(9);
        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();
        String text = message.getText();
        InlineKeyboardMarkup replyMarkup = message.getReplyMarkup();

        InlineKeyboardButton inlineKeyboardButton =
                message.getReplyMarkup().getKeyboard().get(0).get(0);
        inlineKeyboardButton.setCallbackData("noData");
        inlineKeyboardButton.setText("✅ archived");

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(messageId);
        editMessageText.setText(text);
        editMessageText.setReplyMarkup(replyMarkup);
        wordRepository.wordToArchive(chatId, wordToArchive);

        return editMessageText;
    }

    @Override
    public EditMessageText wordToList(String data, Message message) {
        int messageId = message.getMessageId();
        String wordToMove = data.substring(6);
        String messageText = message.getText();
        long studentId = message.getChatId();
        wordRepository.wordToList(studentId, wordToMove);

        InlineKeyboardButton inlineKeyboardButton =
                message.getReplyMarkup().getKeyboard().get(0).get(0);
        inlineKeyboardButton.setCallbackData("noData");
        inlineKeyboardButton.setText("✅ moved to list");

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(studentId));
        editMessageText.setMessageId(messageId);
        editMessageText.setText(messageText);
        editMessageText.setReplyMarkup(message.getReplyMarkup());
        return editMessageText;
    }

    @Override
    public EditMessageReplyMarkup wordListen(String data, Message message) {
        InlineKeyboardMarkup replyMarkup = message.getReplyMarkup();
        InlineKeyboardButton inlineKeyboardButton = replyMarkup.getKeyboard().get(0).get(1);
        inlineKeyboardButton.setText("soon:)");
        inlineKeyboardButton.setCallbackData("noData");
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setReplyMarkup(replyMarkup);
        editMessageReplyMarkup.setChatId(message.getChatId());
        editMessageReplyMarkup.setMessageId(message.getMessageId());
        return editMessageReplyMarkup;
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
    public List<SendMessage> getLastWordsAndHomeTask() {
        List<Word> lastWords = wordRepository.getLastWords(currentStudentId, wordsOnPage);
        if (lastWords.isEmpty()) {
            return List.of(new SendMessage(String.valueOf(adminId), "found 0 words"));
        }
        StringBuilder reply = new StringBuilder();
        reply
                .append("list of last ")
                .append(wordsOnPage)
                .append(" words:")
                .append("\n");
        for (Word word : lastWords) {
            reply
                    .append(word.getId())
                    .append(". ")
                    .append(word.getWordEnglish())
                    .append(" - ")
                    .append(word.getWordOriginal())
                    .append("\n");
        }
        Optional<Homework> homeworkOptional = homeTaskRepository.findHomeTaskById(currentStudentId);
        if (homeworkOptional.isPresent()) {
            reply.append("Last homework: ")
                    .append(homeworkOptional.get().getDescription());
        }
        return List.of(new SendMessage(String.valueOf(adminId), reply.toString()));
    }

    @Override
    public List<SendMessage> addWord(String text, long currentId) {
        String wordEnglish = text.substring(1, text.lastIndexOf("+")).replaceAll("[.!]", "").trim().toLowerCase();
        String wordOrigin = text.substring(text.lastIndexOf("+") + 1).replaceAll("[.!]", "").trim().toLowerCase();
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
    public List<SendMessage> initializeNewStudent(Message message, long currentId) {
        Student student = new Student(message.getChatId(), message.getFrom().getFirstName(),
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
    public void switchToAdminChat() {
        if (currentStudentId != adminId) {
            currentStudentId = adminId;
        }
    }

    @Override
    public List<SendMessage> printStudentList(long chatId) {
        List<Word> studentWords = wordRepository.getAllStudentWords(chatId);
        if (studentWords.isEmpty()) {
            return List.of(new SendMessage(String.valueOf(chatId), "found 0 words"));
        }
        StringBuilder reply = new StringBuilder();
        reply
                .append("list of your words: ")
                .append("\n");

        List<SendMessage> messages = new LinkedList<>();

        for (int i = 1; i <= studentWords.size(); i++) {
            reply
                    .append(i)
                    .append(". ")
                    .append(studentWords.get(i - 1).getWordEnglish())
                    .append(" - ")
                    .append(studentWords.get(i - 1).getWordOriginal())
                    .append("\n");
            if (i % wordsOnPage == 0) {
                messages.add(new SendMessage(String.valueOf(chatId), reply.toString()));
                reply.setLength(0);
            }
        }
        Optional<Homework> homeworkOptional = homeTaskRepository.findHomeTaskById(chatId);
        if (homeworkOptional.isPresent()) {
            reply.append("Last homework: ")
                    .append(homeworkOptional.get().getDescription());
        }
        messages.add(new SendMessage(String.valueOf(chatId), reply.toString()));

        return messages;
    }

    @Override
    public List<SendMessage> handleStudentMessage(long studentId, String messageText) {
        System.out.println(messageText);
        if (!cache.cacheCheck(studentId)) {
            return List.of(new SendMessage(String.valueOf(studentId), generator.waitMessage()));
        }
        Word lastWord = cache.get(studentId);
        cache.remove(studentId);
        List<SendMessage> messagesForStudent = new ArrayList<>();

        if (lastWord.getWordEnglish().equals(messageText.trim().toLowerCase())) {
            messagesForStudent.add(new SendMessage(String.valueOf(studentId), "✅ " + generator.correctMessage()));
        } else {
            messagesForStudent.add(new SendMessage(String.valueOf(studentId), "❌ " + generator.wrongMessage() + " Correct answer \n" + "✅ " + lastWord.getWordEnglish()));
        }
        if (lastWord.getGroupName().equals("bot")) {
            return messagesForStudent;
        }
        if (cacheList.isEmpty(studentId)) {
            SendMessage sendMessage = new SendMessage(String.valueOf(studentId), "end of the list");
            messagesForStudent.add(sendMessage);
            return messagesForStudent;
        }
        Word anyWordFromList = cacheList.getAndDelete(studentId);
        cache.put(studentId, anyWordFromList);
        if (anyWordFromList.getGroupName().equals("new")) {
            messagesForStudent.add(new SendMessage(String.valueOf(studentId),
                    generator.askMessage() + anyWordFromList.getWordOriginal()));
            return messagesForStudent;
        }
        if (anyWordFromList.getGroupName().equals("all")) {
            SendMessage sendMessage = new SendMessage(String.valueOf(studentId), generator.askMessage() + anyWordFromList.getWordOriginal());
            sendMessage.setReplyMarkup(keyboards.getAllWordButtons(anyWordFromList.getWordEnglish()));
            messagesForStudent.add(sendMessage);
            return messagesForStudent;
        }
        if (anyWordFromList.getGroupName().equals("archive")) {
            SendMessage sendMessage = new SendMessage(String.valueOf(studentId), generator.askMessage() + anyWordFromList.getWordOriginal());
            sendMessage.setReplyMarkup(keyboards.getArchiveWordButtons(anyWordFromList.getWordEnglish()));
            messagesForStudent.add(sendMessage);
            return messagesForStudent;
        }
        if (anyWordFromList.getGroupName().equals("collocations1")) {
            messagesForStudent.add(new SendMessage(String.valueOf(studentId),
                    generator.askMessage() + anyWordFromList.getWordOriginal()));
            return messagesForStudent;
        }
        if (anyWordFromList.getGroupName().equals("do") || anyWordFromList.getGroupName().equals("make")) {
            SendMessage sendMessage = new SendMessage(String.valueOf(studentId), anyWordFromList.getWordOriginal().substring(anyWordFromList.getWordOriginal().indexOf(" ")));
            sendMessage.setReplyMarkup(keyboards.getDoMakeButtons());
            messagesForStudent.add(sendMessage);
            return messagesForStudent;
        }
        return messagesForStudent;
    }
}
