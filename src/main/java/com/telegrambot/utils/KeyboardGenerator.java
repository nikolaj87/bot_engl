package com.telegrambot.utils;

import com.telegrambot.entity.Student;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Component
public class KeyboardGenerator {
    private final ReplyKeyboardMarkup replyKeyboardMarkup;
    private final InlineKeyboardMarkup inlineKeyboardMarkup;

    {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add("new words");
        row.add("all words");
        row.add("stop");

        keyboardRows.add(row);
        keyboard.setKeyboard(keyboardRows);
        replyKeyboardMarkup = keyboard;
    }

    {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton yes = new InlineKeyboardButton("YES");
        InlineKeyboardButton no = new InlineKeyboardButton("NO");
        yes.setCallbackData("hwYes");
        no.setCallbackData("hwNo");
        keyboardMarkup.setKeyboard(List.of(List.of(yes, no)));
        inlineKeyboardMarkup = keyboardMarkup;
    }

    public ReplyKeyboardMarkup getMainMenuKeyboard() {
//        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
//        keyboard.setResizeKeyboard(true);
//        keyboard.setOneTimeKeyboard(false);
//
//        List<KeyboardRow> keyboardRows = new ArrayList<>();
//
//        KeyboardRow row = new KeyboardRow();
//        row.add("study new");
//        row.add("all words");
//
//        keyboardRows.add(row);
//        keyboard.setKeyboard(keyboardRows);
        return replyKeyboardMarkup;
    }

    public InlineKeyboardMarkup getYesNoBoard() {
//        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
//        InlineKeyboardButton yes = new InlineKeyboardButton("YES");
//        InlineKeyboardButton no = new InlineKeyboardButton("NO");
//        yes.setCallbackData("buttonYes");
//        no.setCallbackData("buttonNo");
//        keyboardMarkup.setKeyboard(List.of(List.of(yes, no)));
        return inlineKeyboardMarkup;
    }


    public List<SendMessage> generateStudentList(List<Student> allStudents, long adminId) {

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        StringBuilder messageForAdmin = new StringBuilder();
        List<List<InlineKeyboardButton>> allKeys = new LinkedList<>();
        List<InlineKeyboardButton> buttonRow = new LinkedList<>();
        InlineKeyboardButton button;

        for (int i = 1; i <= allStudents.size(); i++) {
            messageForAdmin
                    .append((i))
                    .append("  ")
                    .append(allStudents.get(i - 1).getName())
                    .append("\n");
            button = new InlineKeyboardButton(String.valueOf(i));
            button.setCallbackData("student" + allStudents.get(i - 1).getId());
            buttonRow.add(button);

            if (i % 4 == 0) {
                allKeys.add(buttonRow);
                buttonRow = new LinkedList<>();
            }

        }
        if (!buttonRow.isEmpty()) {
            allKeys.add(buttonRow);
        }
        keyboardMarkup.setKeyboard(allKeys);
        SendMessage studentsForAdmin = new SendMessage(String.valueOf(adminId), String.valueOf(messageForAdmin));
        studentsForAdmin.setReplyMarkup(keyboardMarkup);
        return List.of(studentsForAdmin);
    }
}
