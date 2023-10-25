package com.telegrambot.service;

import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface StudentService {
    @Transactional
    public List<SendMessage> addWord(String text);
    @Transactional
    public List<SendMessage> initializeNewMember(Update update);
    @Transactional
    public List<SendMessage> addHomeTask(String text);
    @Transactional
    public List<SendMessage> switchStudent(String text);
    }
