package com.telegrambot.service;

import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface Service {
    @Transactional
    List<SendMessage> addWord(String text, long currentId);
    @Transactional
    List<SendMessage> initializeNewStudent(Update update, long currentId);
    @Transactional
    List<SendMessage> addHomeTask(long studentId, String text);
    @Transactional(readOnly = true)
    List<SendMessage> switchStudent(String text);
    @Transactional(readOnly = true)
    List<SendMessage> createTaskList();
    @Transactional
    List<SendMessage> handleStudentMessage(long studentId, String messageText);
    @Transactional(readOnly = true)
    List<SendMessage> studyNewButton(long chatId, String messageText);
    @Transactional
    List<SendMessage> studyAllButton(long chatId, String messageText);
    @Transactional
    List<SendMessage> homeWorkRemind();
    @Transactional
    List<SendMessage> handleHomeworkReply(long studentId, String reply);
    @Transactional
    List<SendMessage> getAllStudents(Update update);
    @Transactional
    List<SendMessage> getLastWords();
    @Transactional
    List<SendMessage> clearCache(long chatId);
    @Transactional
    List<SendMessage> studyArchiveButton(long chatId, String messageText);
    @Transactional
    List<SendMessage> wordToArchive(long studentId, String word);
    @Transactional
    List<SendMessage> wordToList(long studentId, String word);
    @Transactional
    List<SendMessage> wordListen(long studentId);
    @Transactional
    List<SendMessage> deleteById(String request);
    @Transactional
    List<SendMessage> studyCollocationsButton(long chatId);
}
