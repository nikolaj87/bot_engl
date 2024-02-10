package com.telegrambot.bot;

import com.telegrambot.config.BotConfig;
import com.telegrambot.service.AdminService;
import com.telegrambot.service.GptService;
import com.telegrambot.service.ServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
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
    private final AdminService adminService;
    private final GptService gptService;


    @Value("${admin_id}")
    private long adminId;
    private static final String REG_EX_ADD_WORD = "\\+.{2,}\\+.{2,}";       //  +...+...

    public TelegramBot(BotConfig botConfig, ServiceImpl service, AdminService adminService, GptService gptService) {
        this.botConfig = botConfig;
        this.service = service;
        this.adminService = adminService;
        this.gptService = gptService;
        List<BotCommand> commandList = new ArrayList<>();
        commandList.add(new BotCommand("/print_list", "show me my list!"));
        commandList.add(new BotCommand("/my_words", "ALL WORDS"));
        commandList.add(new BotCommand("/last_words", "NEW WORDS"));
        commandList.add(new BotCommand("/archive", "ARCHIVE"));
        commandList.add(new BotCommand("/do_make", "study do_make"));
        commandList.add(new BotCommand("/collocations", "study collocations"));
//        commandList.add(new BotCommand("/stop", "stop studying words"));
        commandList.add(new BotCommand("/start", "INFO"));
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

    public void switchToAdminChat() {
        service.switchToAdminChat();
    }

    private List<SendMessage> handleUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();

            if (chatId == adminId) {
                if (messageText.equalsIgnoreCase("admin")) {
                    return adminService.adminCommand();
                }
                if (messageText.toLowerCase().matches("test\\s?\\d*")) {
                    return gptService.createFillTheGaps(messageText);
                }
                if (messageText.toLowerCase().startsWith("hwhw")) {
                    return service.addHomeTask(chatId, messageText);
                }
                if (messageText.toLowerCase().startsWith("swsw")) {
                    return service.getLastWordsAndHomeTask();
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
            if (messageText.equals("/do_make")) {
                return service.studyDoMakeButton(chatId);
            }
            if (messageText.equals("/stop")) {
                return service.clearCache(chatId);
            }
            if (messageText.equals("/print_list")) {
                return service.printStudentList(chatId);
            }
            if (messageText.equals("/start")) {
                return service.initializeNewStudent(update.getMessage(), chatId);
            }
            if (messageText.toLowerCase().startsWith("gpt ")) {
                return gptService.createContext(chatId, messageText);
            }
            return service.handleStudentMessage(chatId, messageText);
        }
        if (update.hasCallbackQuery()) {
            long studentId = update.getCallbackQuery().getFrom().getId();
            String data = update.getCallbackQuery().getData();
            Message message = update.getCallbackQuery().getMessage();

            if (data.equals("noData")){
                return null;
            }
            if (data.startsWith("hw")) {
                return service.handleHomeworkReply(studentId, data);
            }
            if (data.startsWith("student")) {
                return adminService.switchStudent(data);
            }
            if (data.startsWith("toArchive")) {
                EditMessageText editMessageText = service.wordToArchive(data, message);
                editMessage(editMessageText);
                return null;
            }
            if (data.startsWith("toList")) {
                EditMessageText editMessageText = service.wordToList(data, message);
                editMessage(editMessageText);
                return null;
            }
            if (data.equals("listen")) {
                EditMessageReplyMarkup editMessageReplyMarkup = service.wordListen(data, message);
                editKeyboard(editMessageReplyMarkup);
                return null;
            }
            if (data.startsWith("doMake")) {
                return service.handleStudentMessage(studentId, data.substring(6));
            }
            if (data.startsWith("collocations")) {
                return service.studyCollocationsButtonPage(studentId, data);
            }
        }
        long chatId = update.getMessage().getChatId();
        return List.of(new SendMessage(String.valueOf(chatId), ";-)"));
    }

    private void editMessage (EditMessageText editMessageText) {
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void editKeyboard (EditMessageReplyMarkup editKeyboard) {
        try {
            execute(editKeyboard);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
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
