package com.telegrambot.service;

import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface Service {
    @Transactional
    List<SendMessage> addWord(String text, long currentId);
    @Transactional
    List<SendMessage> initializeNewStudent(Message message, long currentId);
    @Transactional
    List<SendMessage> addHomeTask(long studentId, String text);
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
    List<SendMessage> getLastWordsAndHomeTask();
    @Transactional
    List<SendMessage> clearCache(long chatId);
    @Transactional
    List<SendMessage> studyArchiveButton(long chatId, String messageText);
    @Transactional
    EditMessageText wordToArchive(String data, Message message);
    @Transactional
    EditMessageText wordToList(String data, Message message);
    @Transactional
    EditMessageReplyMarkup wordListen(String data, Message message);
    @Transactional
    List<SendMessage> deleteById(String request);
    @Transactional
    List<SendMessage> studyCollocationsButton(long chatId);
    @Transactional
    List<SendMessage> studyCollocationsButtonPage(long chatId, String data);
    @Transactional
    List<SendMessage> studyDoMakeButton(long chatId);
    @Transactional
    void switchToAdminChat();
    @Transactional
    List<SendMessage> printStudentList(long chatId);
}
