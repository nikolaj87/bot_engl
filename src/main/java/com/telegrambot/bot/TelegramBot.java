package com.telegrambot.bot;

import com.telegrambot.config.BotConfig;
import com.telegrambot.generator.MessageGenerator;
import com.telegrambot.service.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
@EnableScheduling
@AllArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;

    private final ServiceImpl service;

    private final MessageGenerator generator;
    private static final long adminId = 5201447988L;
    private static final String REG_EX_ADD_WORD = "\\+.+\\+.+";       //  +...+...
    private static final String REG_EX_SWITCH_STUDENT = "\\*\\*\\*.+";//  *** ...
    private static final String REG_EX_HOME_WORK = "\\*hw\\s.*";      //  *hw ...

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        List<SendMessage> messages = handleUpdate(update);
        sendMessages(messages);
    }

    public void sendTasks() {
        List<SendMessage> messages = service.createTaskList();
        sendMessages(messages);
    }

    private List<SendMessage> handleUpdate(Update update) {
        long chatId = update.getMessage().getChatId();

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            System.out.println(messageText);
//            if (update.getMessage().getChatId() == adminId) {
                if (messageText.matches(REG_EX_SWITCH_STUDENT)) {
                    return service.switchStudent(messageText);
                }
                if (messageText.matches(REG_EX_ADD_WORD)) {
                    return service.addWord(messageText);
                }
                if (messageText.matches(REG_EX_HOME_WORK)) {
                    return service.addHomeTask(messageText);
                }
//                return List.of(new SendMessage(String.valueOf(ServiceImpl.getStudentId()), messageText));
//            } else {
                if (messageText.equals("/start")) {
                    return service.initializeNewMember(update);
                }
                return service.handleStudentMessage(chatId, messageText);
            }
//        }
        return List.of(new SendMessage(String.valueOf(chatId), generator.waitMessage()));
    }

    private void sendMessages(List<SendMessage> messages) {
        messages.forEach(message -> {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
