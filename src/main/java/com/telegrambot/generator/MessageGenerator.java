package com.telegrambot.generator;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class MessageGenerator {
    private final Random random = new Random();
    private final String[] waitMessages = {
            "see you soon :)",
            "be prepare for the lesson",
            "do not forget your home work",
            "practice, practice, practice..."
    };
    private final String[] correctMessages = {
            "super! :)",
            "all right!"
    };
    private final String[] wrongMessages = {
            "no, no, no!",
            "wrong!"
    };
    private final String[] askMessages = {
            "translate, please - ",
            "how to say in english - "
    };

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
}
