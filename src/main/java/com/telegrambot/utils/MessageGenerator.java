package com.telegrambot.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class MessageGenerator {

    private final Random random = new Random();
    //когда студент пишет что то в чат но уже после  урока
    private final String[] waitMessages = {
            "see you soon :)",
            "be prepared for the lesson",
            "don't forget your homework",
            "practice, practice, practice..."
    };
    //когда студент дает правильный ответ
    private final String[] correctMessages = {
            "super! :)",
            "all right!",
            "keep it up!",
            "good job!",
            "that's right!",
            "that's correct!",
            "way to go!",
            "you got it right!",
            "keep up the good work!"
    };
    //студент дал неправильный ответ
    private final String[] wrongMessages = {
            "no, no, no!",
            "wrong!",
            "try again.",
            "unfortunately, no."
    };
    //бот просит студента перевести фразу
    private final String[] askMessages = {
            "translate please - ",
            "how do you say in English - ",
            "what's the English for - "
    };
    //студент получил домашнее задание
    private final String[] homeworkCreated = {
            "You have new homework! "
    };
    //бот напоминает что нужно сделать домашнее задание
    private final String[] homeworkRemind = {
            "Have you done your homework? ",
            "Is the homework done?: "
    };
    //это студент не получает это не важно
    private final String commandsMessage =
            "+иностранный язык +родной язык  " +
                    "\nпробелы вокруг + не важны " +
                    "\n\nhwhw домашняя работа" +
                    "\nрегистр не важен" +
                    "\n\nswsw - посмотреть последние 30 слов студента" +
                    "\n\ndltНОМЕР - удалить слово по номеру. Номер можно узнать из команды swsw";

    //студент не сделал домашку и бот говорит ему ок я напомню тебе завтра
    private final String[] remindTomorrowHomework = {
            "Ok! I will remind you tomorrow"
    };
    //бот говорит студенту что учитель подключился к уроку
    private final String[] teacherEntersChat = {
            "Alexandra entered the chat"
    };
    private final String[] teacherLeftChat = {
            "Alexandra has left the chat"
    };
    //студент шелкает на уроке команды - например учить слова и бот говорит позже поучишь
    private final String[] laterMessage = {
            "try after the lesson "
    };


    private final String ukr =
            """
                    UKR
                    як використовувати бот:
                                        
                    - меню NEW WORDS
                        це вашi слова за останнi 14 днiв
                                        
                    - меню ALL WORDS
                        це всi вашi слова за весь час
                        якщо ви гарно вивчили слово, то його можно вiдправити до архiву i це слово бiльше не буде з`являтися в списку всiх слiв.
                                        
                    - меню ARCHIVE
                        це список слiв, вiдправленних в архiв
                        Слово можна вiдновити з архiву та вiдправити до списку слiв.
                                        
                    чи можна додати собi слово? Так! Необхiдно написати:
                        +word_in_english+translation
                        наприклад: +dog+собака
                        тепер нове слово додано до вашого списку
                                        
                    - меню stop
                        бот не питатиме бiльше слiв
                                        
                    щодня бот буде питати кiлька слiв. Також слова можна вивчати самостiйно, використовуючи MENU
                                        
                    """;

    private final String pol =
            """
                    POL
                    jak korzystać z bota:

                    - menu NEW WORDS
                        to są Twoje słowa z ostatnich 14 dni

                    - menu ALL WORDS
                        to są wszystkie Twoje słowa z dowolnego okresu. Jeśli nauczyłeś się dobrze słowa, możesz je przenieść do archiwum i to słowo nie będzie już pojawiać się na ogólnej liście

                    - menu ARCHIVE
                        to jest lista słów, wysłanych do archiwum. Słowo można przywrócić z archiwum i wysłać z powrotem na listę

                    - menu stop
                        bot przestanie pytać o słowa

                    czy można dodać własne słowo? Tak, można! Należy napisać:
                        +word_in_english+translation
                        na przykład: +dog+pies
                        teraz to słowo będzie na Twojej liście

                    każdego dnia bot będzie pytać o kilka słów. Można również uczyć się słów samodzielnie, korzystając z MENU

                    """;

    private final String rus =
            """
                    RUS
                    как использовать бот:
                                        
                    - меню NEW WORDS
                        это ваши слова за последние 14 дней
                        
                    - меню ALL WORDS
                        это все ваши слова за любое время. Eсли вы хорошо выучили слово, то его можно отправить в архив и это слово больше не будет появляться в общем списке
                                        
                    - меню ARCHIVE
                        это список слов, отправленных в архив. Слово можно восстановить из архива и отправить обратно в список
                                        
                    - меню stop
                        бот перестанет спрашивать слова

                    можно ли себе добавить слово? Да, можно! Нужно написать:
                        +word_in_english+translation
                        например: +dog+собака
                        теперь это слово будет в вашем списке

                    каждый день бот будет спрашивать несколько слов. Также можно самому их учить исспользуя MENU""";

    public String homeworkMessage() {
        return homeworkCreated[random.nextInt(homeworkCreated.length)];
    }

    public String waitMessage() {
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

    public String teacherLeftChat() {
        return teacherLeftChat[random.nextInt(teacherLeftChat.length)];
    }

    public String commandsMessage() {
        return commandsMessage;
    }

    public String laterMessage() {
        return laterMessage[random.nextInt(laterMessage.length)];
    }

    public String rusMessage() {
        return rus;
    }

    public String ukrMessage() {
        return ukr;
    }

    public String polMessage() {
        return pol;
    }
}
