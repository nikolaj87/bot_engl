package com.telegrambot.service;

import com.telegrambot.entity.Homework;
import com.telegrambot.entity.Student;
import com.telegrambot.entity.Word;
import com.telegrambot.repository.HomeTaskRepository;
import com.telegrambot.repository.StudentRepository;
import com.telegrambot.repository.WordRepository;
import com.telegrambot.utils.KeyboardGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class ServiceImplTest {

    @Autowired
    private ServiceImpl service;
    @Autowired
    private HomeTaskRepository homeTaskRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private WordRepository wordRepository;
    @Autowired
    private KeyboardGenerator keyboard;

    private final long testChatId = 12345L;
    private final long testChatId2 = 777L;

    @Value("${admin_id}")
    private long adminId;

    @BeforeEach
    public void before() {
        studentRepository.save(new Student(testChatId, "Nick", new Timestamp(System.currentTimeMillis())));
        studentRepository.save(new Student(testChatId2, "Vik", new Timestamp(System.currentTimeMillis())));
        homeTaskRepository.save(new Homework(testChatId, "hometaskNick", 1));
        homeTaskRepository.save(new Homework(testChatId2, "hometaskVik", 1));
    }

    @AfterEach
    void tearDown() {
        homeTaskRepository.deleteAll();
        wordRepository.deleteAll();
        studentRepository.deleteAll();
    }


    @Test
    void mustAddHomeTaskForCurrentStudent() {
        String homeTaskButton = "hwhw";
        String homeTaskText = "some text home task";
        service.switchStudent("student" + testChatId + " " + "TestName");

        List<SendMessage> messages = service.addHomeTask(testChatId, homeTaskButton + homeTaskText);
        String result = homeTaskRepository.findHomeTaskById(testChatId).get().getDescription();

        assertEquals(homeTaskText, result);
        assertTrue(messages.get(0).getText().contains(homeTaskText));
        assertEquals(messages.get(0).getChatId(), String.valueOf(testChatId));
        assertTrue(messages.get(1).getText().contains(homeTaskText));
    }

    @Test
    void mustSetHomeTaskIsNotActive() {
        String reply = "hwYes";

        service.handleHomeworkReply(testChatId, reply);
        int isActive = homeTaskRepository.findHomeTaskById(testChatId).get().isActive();

        assertEquals(0, isActive);
    }

    @Test
    void mustCreateHomeworkMessage() {
        Homework homework = new Homework(testChatId, "new homework", 1);
        homeTaskRepository.save(homework);
        service.switchStudent("student" + adminId + " " + "TestName");

        List<SendMessage> sendMessages = service.homeWorkRemind();
        String result = sendMessages.stream()
                .filter(sendMessage -> sendMessage.getChatId().equals(String.valueOf(testChatId)))
                .map(message -> message.getText())
                .findFirst().get();

        assertTrue(result.contains(homework.getDescription()));
    }

    @Test
    void mustSendWordToEveryStudent() {
        Word savedWord1 = wordRepository.save(new Word(0L, "Cat", "Кот", testChatId, null, 0));
        Word savedWord2 = wordRepository.save(new Word(0L, "Dog", "Собака", testChatId2, null, 0));

        List<SendMessage> messages = service.createTaskList();
        String result = messages.stream()
                .filter(message -> message.getChatId().equals(String.valueOf(testChatId)))
                .map(message -> message.getText())
                .findFirst().get();

        assertTrue(result.contains(savedWord1.getWordOriginal()));
    }

    //    @Test
//    void handleStudentMessage() {
//    }
//
    @Test
    void mustFindOnlyNewWord() {
        Word newWord = new Word(0L, "hello", "привет", testChatId,
                new Timestamp(System.currentTimeMillis()), 0);
        Timestamp oldTime = new Timestamp(Instant.now().minus(30, ChronoUnit.DAYS).toEpochMilli());
        Word oldWord = new Word(0L, "bue", "пока", testChatId, oldTime, 0);
        wordRepository.save(newWord);
        wordRepository.save(oldWord);

        List<SendMessage> messages = service.studyNewButton(testChatId, "someText");
        String result1 = messages.get(0).getText();
        String result2 = messages.get(1).getText();

        assertEquals(2, messages.size());
        assertEquals("1 words found", result1);
        assertTrue(result2.contains(newWord.getWordOriginal()));
    }

    @Test
    void mustIgnoreArchiveWord() {
        Word word = new Word(0L, "zebra", "зебра", testChatId,
                new Timestamp(System.currentTimeMillis()), 0);
        Word archivedWord = new Word(0L, "cobra", "кобра", testChatId,
                new Timestamp(System.currentTimeMillis()), 1);
        wordRepository.save(word);
        wordRepository.save(archivedWord);

        List<SendMessage> messages = service.studyAllButton(testChatId, "some text");
        String result1 = messages.get(0).getText();
        String result2 = messages.get(1).getText();

        assertEquals(2, messages.size());
        assertEquals("1 words found", result1);
        assertTrue(result2.contains(word.getWordOriginal()));
    }

    @Test
    void mustFindArchiveWords() {
        Word archivedWord = new Word(0L, "hamster", "хомяк", testChatId,
                new Timestamp(System.currentTimeMillis()), 1);
        Word notArchived = new Word(0L, "cobra", "кобра", testChatId,
                new Timestamp(System.currentTimeMillis()), 0);
        wordRepository.save(archivedWord);
        wordRepository.save(notArchived);

        List<SendMessage> messages = service.studyArchiveButton(testChatId, "some text");
        String result1 = messages.get(0).getText();
        String result2 = messages.get(1).getText();

        assertEquals(2, messages.size());
        assertEquals("1 words found", result1);
        assertTrue(result2.contains(archivedWord.getWordOriginal()));
    }

    @Test
    void mustFindWordCollocationCategory() {
        Word collocationWord = new Word(0L, "make money", "зарабатывать деньги", testChatId,
                new Timestamp(System.currentTimeMillis()), 0, "collocations1");
        Word word = new Word(0L, "tiger", "тигр", testChatId,
                new Timestamp(System.currentTimeMillis()), 0);
        wordRepository.save(collocationWord);
        wordRepository.save(word);
        String collocationButton = "collocations1";

        List<SendMessage> messages = service.studyCollocationsButtonPage(testChatId, collocationButton);
        String result1 = messages.get(0).getText();
        String result2 = messages.get(1).getText();

        assertEquals(2, messages.size());
        assertEquals("1 words found", result1);
        assertTrue(result2.contains(collocationWord.getWordOriginal()));
    }


    @Test
    void mustFindWordDoCategory() {
        Word doWord = new Word(0L, "do research", "исследовать", testChatId,
                new Timestamp(System.currentTimeMillis()), 0, "do");
        Word word = new Word(0L, "shark", "акула", testChatId,
                new Timestamp(System.currentTimeMillis()), 0, "some_group");
        wordRepository.save(doWord);
        wordRepository.save(word);

        List<SendMessage> messages = service.studyDoMakeButton(testChatId);
        String result1 = messages.get(0).getText();
        String result2 = messages.get(2).getText();

        assertEquals(3, messages.size());
        assertEquals("1 words found", result1);
        assertTrue(doWord.getWordEnglish().contains(result2));
    }

    @Test
    void wordToArchive() {
//        Word notArchivedWord = new Word(0L, "lion", "лев", testChatId,
//                new Timestamp(System.currentTimeMillis()), 0);
//        Word savedWord = wordRepository.save(notArchivedWord);
//        Update update = new Update();
//        CallbackQuery callbackQuery = new CallbackQuery();
//        callbackQuery.setData("toArchive" + notArchivedWord.getWordEnglish());
//        update.setCallbackQuery(callbackQuery);
//        Message message = new Message();
//        message.setReplyMarkup((InlineKeyboardMarkup) keyboard.getAllWordButtons(notArchivedWord.getWordEnglish()));
//        message.setChat(new Chat(777L, "some"));
//        callbackQuery.setMessage(message);
//
//
//        service.wordToArchive(update);
//        Word result = wordRepository.findById(savedWord.getId()).get();
//
//        assertEquals(1, result.getIsArchive());
    }


//    @Test
//    void deleteById() {
//    }
//

}