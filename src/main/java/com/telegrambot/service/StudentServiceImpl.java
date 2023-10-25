package com.telegrambot.service;

import com.telegrambot.entity.HomeTask;
import com.telegrambot.entity.Student;
import com.telegrambot.entity.Word;
import com.telegrambot.repository.HomeTaskRepository;
import com.telegrambot.repository.StudentRepository;
import com.telegrambot.repository.WordRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Optional;

@Service
public class StudentServiceImpl implements StudentService{

    private final StudentRepository studentRepository;
    private final WordRepository wordRepository;
    private final HomeTaskRepository homeTaskRepository;
    private static long currentStudentId = 5201447988L;
    private static final long adminId = 5201447988L;

    private static final String UKRAINIAN_FLAG = "\uD83C\uDDFA\uD83C\uDDE6";

    public StudentServiceImpl(StudentRepository studentRepository, WordRepository wordRepository, HomeTaskRepository homeTaskRepository) {
        this.studentRepository = studentRepository;
        this.wordRepository = wordRepository;
        this.homeTaskRepository = homeTaskRepository;
    }
    public static long getStudentId () {
        return currentStudentId;
    }

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

    public List<SendMessage> addHomeTask(String messageText) {
        String homeWorkText = messageText.substring(4);
        HomeTask homeTask = new HomeTask(0L, currentStudentId, homeWorkText, true);
        homeTaskRepository.save(homeTask);
        return List.of(new SendMessage(String.valueOf(currentStudentId), homeWorkText));
    }

    public List<SendMessage> initializeNewMember(Update update) {
        Student student = new Student(update.getMessage().getChatId(), update.getMessage().getFrom().getFirstName(),
                null, null);
        studentRepository.save(student);
        return List.of(new SendMessage(String.valueOf(adminId), "студент подключился id = " +
                student.getId() + " имя = " + student.getName()));
    }

    public List<SendMessage> addWord(String text) {
        String wordEnglish = text.substring(1, text.lastIndexOf("+"));
        String wordOrigin  = text.substring(text.lastIndexOf("+") + 1);
        Word word = new Word(0L, wordEnglish, wordOrigin, currentStudentId);
        wordRepository.save(word);
        return List.of(new SendMessage(String.valueOf(currentStudentId), wordOrigin + " - a new word to learn"));
    }


}
