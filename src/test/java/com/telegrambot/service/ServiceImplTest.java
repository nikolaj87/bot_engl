package com.telegrambot.service;

import com.telegrambot.entity.Homework;
import com.telegrambot.entity.Student;
import com.telegrambot.repository.HomeTaskRepository;
import com.telegrambot.repository.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class ServiceImplTest {

    @Autowired
    private ServiceImpl service;
    @Autowired
    private HomeTaskRepository homeTaskRepository;
    @Autowired
    private StudentRepository studentRepository;

    private final long testChatId = 12345L;

    @Value("${admin_id}")
    private long adminId;

    @BeforeEach
    public void beforeAll() {
        studentRepository.save(new Student(testChatId, "Test", new Timestamp(System.currentTimeMillis())));
        homeTaskRepository.save(new Homework(testChatId, "hometask", 1));
    }

    @AfterEach
    void tearDown() {
    }


    @Test
    void addHomeTask() {
        String homeTaskButton = "hwhw";
        String homeTaskText = "some text home task";

        List<SendMessage> messages = service.addHomeTask(testChatId, homeTaskButton + homeTaskText);
        String result = homeTaskRepository.findHomeTaskById(testChatId).get().getDescription();

        assertEquals(homeTaskText, result);
//        assertTrue(homeTaskText.contains(result));
        assertTrue(messages.get(0).getText().contains(homeTaskText));
        assertEquals(messages.get(0).getChatId(), String.valueOf(testChatId));
        assertTrue(messages.get(1).getText().contains(homeTaskText));
        assertEquals(messages.get(1).getChatId(), String.valueOf(adminId));
    }

    @Test
    void handleHomeworkReply() {
    }

    @Test
    void homeWorkRemind() {
    }

    @Test
    void createTaskList() {
    }

    @Test
    void handleStudentMessage() {
    }

    @Test
    void studyNewButton() {
    }

    @Test
    void studyAllButton() {
    }

    @Test
    void studyArchiveButton() {
    }

    @Test
    void studyCollocationsButton() {
    }

    @Test
    void studyCollocationsButtonPage() {
    }

    @Test
    void studyDoMakeButton() {
    }

    @Test
    void wordToArchive() {
    }

    @Test
    void wordToList() {
    }

    @Test
    void wordListen() {
    }

    @Test
    void deleteById() {
    }

    @Test
    void getLastWordsAndHomeTask() {
    }

    @Test
    void clearCache() {
    }

    @Test
    void addWord() {
    }

    @Test
    void initializeNewStudent() {
    }

    @Test
    void getAllStudents() {
    }

    @Test
    void switchStudent() {
    }

    @Test
    void switchToAdminChat() {
    }

    @Test
    void getStudentId() {
    }

    @Test
    void testAddHomeTask() {
    }

    @Test
    void testHandleHomeworkReply() {
    }

    @Test
    void testHomeWorkRemind() {
    }

    @Test
    void testCreateTaskList() {
    }

    @Test
    void testHandleStudentMessage() {
    }

    @Test
    void testStudyNewButton() {
    }

    @Test
    void testStudyAllButton() {
    }

    @Test
    void testStudyArchiveButton() {
    }

    @Test
    void testStudyCollocationsButton() {
    }

    @Test
    void testStudyCollocationsButtonPage() {
    }

    @Test
    void testStudyDoMakeButton() {
    }

    @Test
    void testWordToArchive() {
    }

    @Test
    void testWordToList() {
    }

    @Test
    void testWordListen() {
    }

    @Test
    void testDeleteById() {
    }

    @Test
    void testGetLastWordsAndHomeTask() {
    }

    @Test
    void testClearCache() {
    }

    @Test
    void testAddWord() {
    }

    @Test
    void testInitializeNewStudent() {
    }

    @Test
    void testGetAllStudents() {
    }

    @Test
    void testSwitchStudent() {
    }

    @Test
    void testSwitchToAdminChat() {
    }
}