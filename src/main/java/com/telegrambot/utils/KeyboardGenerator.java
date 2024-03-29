package com.telegrambot.utils;

import com.telegrambot.entity.Student;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Component
public class KeyboardGenerator {

    private final String speaker = "🔊";

    public InlineKeyboardMarkup getYesNoBoard() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton yes = new InlineKeyboardButton("YES");
        InlineKeyboardButton no = new InlineKeyboardButton("NO");
        yes.setCallbackData("buttonYes");
        no.setCallbackData("buttonNo");
        keyboardMarkup.setKeyboard(List.of(List.of(yes, no)));
        return keyboardMarkup;
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
            Student student = allStudents.get(i - 1);
            button.setCallbackData("student" + student.getId() + " " + student.getName());
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

    public ReplyKeyboard getAllWordButtons(String englishWord) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton toArchive = new InlineKeyboardButton("<<to archive");
        InlineKeyboardButton listen = new InlineKeyboardButton("listen  " + speaker);
        toArchive.setCallbackData("toArchive" + englishWord);
        listen.setCallbackData("listen");
        keyboardMarkup.setKeyboard(List.of(List.of(toArchive, listen)));
        return keyboardMarkup;
    }

    public ReplyKeyboard getDoMakeButtons() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton doButton = new InlineKeyboardButton("do");
        InlineKeyboardButton makeButton = new InlineKeyboardButton("make");
        doButton.setCallbackData("doMakeDo");
        makeButton.setCallbackData("doMakeMake");
        keyboardMarkup.setKeyboard(List.of(List.of(doButton, makeButton)));
        return keyboardMarkup;
    }

    public ReplyKeyboard getArchiveWordButtons(String englishWord) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton listen = new InlineKeyboardButton("listen  " + speaker);
        InlineKeyboardButton toList = new InlineKeyboardButton("to list>>");
        toList.setCallbackData("toList" + englishWord);
        listen.setCallbackData("listen");
        keyboardMarkup.setKeyboard(List.of(List.of(toList, listen)));
        return keyboardMarkup;
    }

    public ReplyKeyboard addPageNumberButtons(int pagesNumber) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> allKeys = new LinkedList<>();
        List<InlineKeyboardButton> buttonRow = new LinkedList<>();
        InlineKeyboardButton button;

        for (int i = 1; i <= pagesNumber; i++) {
            button = new InlineKeyboardButton("page " + i);
            button.setCallbackData("collocations" + i);
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
        return keyboardMarkup;
    }
}
