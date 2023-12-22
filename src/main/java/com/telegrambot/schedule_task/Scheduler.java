package com.telegrambot.schedule_task;

import com.telegrambot.bot.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Scheduler {
    private final TelegramBot telegramBot;
    @Autowired
    public Scheduler(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

//    @Scheduled(cron = "0 0 8,13,17 * * *")
    public void sendTasks () {
        telegramBot.sendTasks();
    }

//    @Scheduled(cron = "0 0 18 * * *")
    public void homeWorkRemind () {
        telegramBot.homeWorkRemind();
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void switchToAdminChat () {
        telegramBot.switchToAdminChat();
    }
}
