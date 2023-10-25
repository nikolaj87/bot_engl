package com.telegrambot.service;

import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface Service {
    @Transactional
    List<SendMessage> addWord(String text);
    @Transactional
    List<SendMessage> initializeNewMember(Update update);
    @Transactional
    List<SendMessage> addHomeTask(String text);
    @Transactional(readOnly = true)
    List<SendMessage> switchStudent(String text);
    @Transactional(readOnly = true)
    List<SendMessage> createTaskList();
    @Transactional
    List<SendMessage> handleStudentMessage(long studentId, String messageText);
}
