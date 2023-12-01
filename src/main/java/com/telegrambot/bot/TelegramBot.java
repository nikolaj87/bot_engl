package com.telegrambot.bot;

import com.telegrambot.config.BotConfig;
import com.telegrambot.utils.MessageGenerator;
import com.telegrambot.service.ServiceImpl;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
@EnableScheduling
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final ServiceImpl service;
    private final MessageGenerator generator;
    private static final long adminId = 795942078L;
    private static final String REG_EX_ADD_WORD = "\\+.{2,}\\+.{2,}";       //  +...+...

    public TelegramBot(BotConfig botConfig, ServiceImpl service, MessageGenerator generator) {
        this.botConfig = botConfig;
        this.service = service;
        this.generator = generator;
        List<BotCommand> commandList = new ArrayList<>();
        commandList.add(new BotCommand("/switch_student", "admin switch student"));
        commandList.add(new BotCommand("/my_words", "ALL WORDS"));
        commandList.add(new BotCommand("/last_words", "NEW WORDS"));
        commandList.add(new BotCommand("/archive", "ARCHIVE"));
        commandList.add(new BotCommand("/collocations", "study collocations"));
        commandList.add(new BotCommand("/stop", "stop studying words"));
        commandList.add(new BotCommand("/start", "INFO"));
//        commandList.add(new BotCommand("/commands", "admin remind commands"));
        try {
            this.execute(new SetMyCommands(commandList, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

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

    public void homeWorkRemind() {
        List<SendMessage> messages = service.homeWorkRemind();
        sendMessages(messages);
    }

    private List<SendMessage> handleUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
//            System.out.println(messageText);

            if (update.getMessage().getChatId() == adminId) {
                if (messageText.toLowerCase().startsWith("hwhw")) {
                    return service.addHomeTask(chatId, messageText);
                }
                if (messageText.equals("/switch_student")) {
                    return service.getAllStudents(update);
                }
                if (messageText.toLowerCase().startsWith("swsw")) {
                    return service.getLastWords();
                }
                if (messageText.toLowerCase().matches("dlt\\s?\\d*")) {
                    return service.deleteById(messageText);
                }
            }
            if (messageText.matches(REG_EX_ADD_WORD)) {
                return service.addWord(messageText, chatId);
            }
            if (messageText.equals("/last_words")) {
                return service.studyNewButton(chatId, messageText);
            }
            if (messageText.equals("/my_words")) {
                return service.studyAllButton(chatId, messageText);
            }
            if (messageText.equals("/archive")) {
                return service.studyArchiveButton(chatId, messageText);
            }
            if (messageText.equals("/collocations")) {
                return service.studyCollocationsButton(chatId);
            }
            if (messageText.equals("/stop")) {
                return service.clearCache(chatId);
            }
            if (messageText.equals("/start")) {
                return service.initializeNewStudent(update, chatId);
            }
            //если учитель пишет студенту просто разрешить это сообщение
            if (chatId == adminId && chatId != ServiceImpl.getStudentId()) {
                return List.of(new SendMessage(String.valueOf(ServiceImpl.getStudentId()), messageText));
            }
            //иначе это ответ на сообщение и надо его проверить
            return service.handleStudentMessage(chatId, messageText);
        }
        if (update.hasCallbackQuery()) {
            long studentId = update.getCallbackQuery().getFrom().getId();
            String data = update.getCallbackQuery().getData();

            if (data.startsWith("hw")) {
                return service.handleHomeworkReply(studentId, data);
            }
            if (data.startsWith("student")) {
                return service.switchStudent(data);
            }
            if (data.startsWith("toArchive")) {
                return service.wordToArchive(studentId, data.substring(9));
            }
            if (data.startsWith("toList")) {
                return service.wordToList(studentId, data.substring(6));
            }
            if (data.equals("listen")) {
                return service.wordListen(studentId);
            }
        }
//        return List.of(new SendMessage("795942078", generator.waitMessage()));
        return null;
    }

    private void sendMessages(List<SendMessage> messages) {
        if (messages != null) {
            messages.forEach(message -> {
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
