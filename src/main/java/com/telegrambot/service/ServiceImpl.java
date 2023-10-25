package com.telegrambot.service;

import com.telegrambot.cache.Cache;
import com.telegrambot.entity.HomeTask;
import com.telegrambot.entity.Student;
import com.telegrambot.entity.Word;
import com.telegrambot.generator.MessageGenerator;
import com.telegrambot.repository.HomeTaskRepository;
import com.telegrambot.repository.StudentRepository;
import com.telegrambot.repository.WordRepository;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ServiceImpl implements Service {

    private final Cache cache;
    private final MessageGenerator generator;
    private final StudentRepository studentRepository;
    private final WordRepository wordRepository;
    private final HomeTaskRepository homeTaskRepository;
    private static long currentStudentId = 5201447988L;
    private static final long adminId = 5201447988L;
    private static final String UKRAINIAN_FLAG = "\uD83C\uDDFA\uD83C\uDDE6";

    public ServiceImpl(Cache cache, MessageGenerator generator, StudentRepository studentRepository, WordRepository wordRepository, HomeTaskRepository homeTaskRepository) {
        this.cache = cache;
        this.generator = generator;
        this.studentRepository = studentRepository;
        this.wordRepository = wordRepository;
        this.homeTaskRepository = homeTaskRepository;
    }

    public static long getStudentId() {
        return currentStudentId;
    }

    public static void main(String[] args) {
        System.out.println("U+2705");
    }
    @Override
    public List<SendMessage> switchStudent(String text) {
        String studentName = text.substring(text.indexOf(" ") + 1);
        Optional<Student> studentByNameOptional = studentRepository.findStudentByName(studentName);
        if (studentByNameOptional.isPresent()) {
            Student student = studentByNameOptional.get();
            currentStudentId = student.getId();
            return List.of(new SendMessage(String.valueOf(adminId), "переключено на студента " + studentName +
                    "! Теперь он получает прямые сообщения и можно сохранить для него слова"));
        }
        return List.of(new SendMessage(String.valueOf(adminId), "нет такого студента!!! попробуй еще раз" + studentName));
    }

    @Override
    public List<SendMessage> createTaskList() {
        cache.cacheEvict();
        List<SendMessage> messages = new LinkedList<>();

        List<Student> allStudents = studentRepository.findAll();
        allStudents.forEach(student -> {
            String anyWordByStudentId = wordRepository.getAnyEnglishWordByStudentId(student.getId());
            if (anyWordByStudentId == null) return;
            cache.put(student.getId(), anyWordByStudentId);
            messages.add(new SendMessage(String.valueOf(student.getId()), generator.askMessage() + anyWordByStudentId));
        });
        return messages;
    }

    @Override
    public List<SendMessage> handleStudentMessage(long studentId, String messageText) {
        if (cache.cacheCheck(studentId)) {
            String englishWord = wordRepository.getEnglishByOriginal(cache.get(studentId));
            cache.remove(studentId);
            if (englishWord.equals(messageText.trim().toLowerCase())) {
                return List.of(new SendMessage(String.valueOf(studentId), "✅ " + generator.correctMessage()));
            } else {
                return List.of(new SendMessage(String.valueOf(studentId), "❌ " + generator.wrongMessage() + " Correct answer - " + "✅ " + englishWord));
            }
        } else {
            return List.of(new SendMessage(String.valueOf(studentId), generator.waitMessage()));
        }
    }

    @Override
    public List<SendMessage> addHomeTask(String messageText) {
        String homeWorkText = messageText.substring(4);
        HomeTask homeTask = new HomeTask(0L, currentStudentId, homeWorkText, true);
        homeTaskRepository.save(homeTask);
        return List.of(new SendMessage(String.valueOf(currentStudentId), homeWorkText));
    }

    @Override
    public List<SendMessage> initializeNewMember(Update update) {
        Student student = new Student(update.getMessage().getChatId(), update.getMessage().getFrom().getFirstName(),
                null, null);
        studentRepository.save(student);
        return List.of(new SendMessage(String.valueOf(adminId), "новый студент подключился id = " +
                student.getId() + " имя = " + student.getName()));
    }

    @Override
    public List<SendMessage> addWord(String text) {
        String wordEnglish = text.substring(1, text.lastIndexOf("+")).trim().toLowerCase();
        String wordOrigin = text.substring(text.lastIndexOf("+") + 1).trim().toLowerCase();
        Word word = new Word(0L, wordEnglish, wordOrigin, currentStudentId);
        wordRepository.save(word);
        return List.of(new SendMessage(String.valueOf(currentStudentId), wordOrigin + " - a new word to learn"));
    }
}
