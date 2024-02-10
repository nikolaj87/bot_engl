package com.telegrambot.service;

import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

public interface GptService {
    @Transactional
    List<SendMessage> createContext(long chatId, String message);
    @Transactional
    List<SendMessage> createFillTheGaps(String messageText);
}
