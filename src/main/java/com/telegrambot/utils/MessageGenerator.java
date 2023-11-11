package com.telegrambot.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class MessageGenerator {

    private final Random random = new Random();
    //когда студент пишет что то в чат но уже после  урока
    private final String[] waitMessages = {
            "see you soon :)",
            "be prepare for the lesson",
            "do not forget your home work",
            "practice, practice, practice..."
    };
    //когда студент дает правильный ответ
    private final String[] correctMessages = {
            "super! :)",
            "all right!"
    };
    //студент дал неправильный ответ
    private final String[] wrongMessages = {
            "no, no, no!",
            "wrong!"
    };
    //бот просит студента перевести фразу
    private final String[] askMessages = {
            "translate, please - ",
            "how to say in english - "
    };
    //студент получил домашнее задание
    private final String[] homeworkCreated = {
            "You have a new homework! "
    };
    //бот напоминает что нужно сделать домашнее задание
    private final String[] homeworkRemind = {
            "Remind your homework: "
    };
    //это студент не получает это не важно
    private final String commandsMessage = "+english text+оригинальный текст \nпробелы вокруг + не важны \n\nhwhw домашняя работа\nрегистр не важен";

    //студент не сделал домашку и бот говорит ему ок я напомню тебе завтра
    private final String[] remindTomorrowHomework = {
            "Ok! I will remind you tomorrow "
    };
    //бот говорит студенту что учитель подключился к уроку
    private final String[] teacherEntersChat = {
            "Alexandra entered the chat "
    };
    //студент шелкает на уроке команды - например учить слова и бот говорит позже поучишь
    private final String[] laterMessage = {
            "try after the lesson "
    };

    public String homeworkMessage () {
        return homeworkCreated[random.nextInt(homeworkCreated.length)];    }
    public String waitMessage () {
        return waitMessages[random.nextInt(waitMessages.length)];
    }

    public String correctMessage() {
        return correctMessages[random.nextInt(correctMessages.length)];
    }

    public String wrongMessage() {
        return wrongMessages[random.nextInt(wrongMessages.length)];
    }

    public String askMessage() {
        return askMessages[random.nextInt(askMessages.length)];
    }
    public String getHomeworkRemind() {
        return homeworkRemind[random.nextInt(homeworkRemind.length)];
    }

    public String remindTomorrowHomework() {
        return remindTomorrowHomework[random.nextInt(remindTomorrowHomework.length)];
    }
    public String teacherEntersChat() {
        return teacherEntersChat[random.nextInt(teacherEntersChat.length)];
    }
    public String commandsMessage() {
        return commandsMessage;
    }

    public String laterMessage() {
        return laterMessage[random.nextInt(laterMessage.length)];
    }
}
