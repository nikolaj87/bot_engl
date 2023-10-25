package com.telegrambot.bot;

import com.telegrambot.config.BotConfig;
import com.telegrambot.entity.Student;
import com.telegrambot.repository.StudentRepository;
import com.telegrambot.repository.WordRepository;
import com.telegrambot.service.StudentServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
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
    private final WordRepository wordRepository;
    private final StudentRepository studentRepository;
    private final StudentServiceImpl service;
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
        messages.forEach(message -> {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Scheduled(cron = "*/30 * * * * *")
    public void sendMessage () {
        List<Student> allStudents = studentRepository.findAll();
        allStudents.forEach(student -> {
            String anyWordByStudentId = wordRepository.getAnyEnglishWordByStudentId(student.getId());
            if (anyWordByStudentId == null) return;
            try {
                execute(new SendMessage(String.valueOf(student.getId()), "how to say in English " +
                        anyWordByStudentId + "?"));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        });
    }

    private List<SendMessage> handleUpdate(Update update) {
        long chatId = update.getMessage().getChatId();

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

            if (update.getMessage().getChatId() == adminId) {
                if (messageText.matches(REG_EX_SWITCH_STUDENT)) {
                    return service.switchStudent(messageText);
                }
                if (messageText.matches(REG_EX_ADD_WORD)) {
                    return service.addWord(messageText);
                }
                if (messageText.matches(REG_EX_HOME_WORK)) {
                    return service.addHomeTask(messageText);
                }
                return List.of(new SendMessage(String.valueOf(StudentServiceImpl.getStudentId()), messageText));
            }
            if (messageText.equals("/start")) {
                return service.initializeNewMember(update);
            }
        }
        return List.of(new SendMessage(String.valueOf(chatId), "see you soon :)"));
    }
}
