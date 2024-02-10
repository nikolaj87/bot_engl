package com.telegrambot.service;

import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

public interface AdminService {
    @Transactional
    List<SendMessage> adminCommand();
    @Transactional
    List<SendMessage> switchStudent(String data);
}
