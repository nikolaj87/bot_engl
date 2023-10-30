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

    @Scheduled(cron = "30 */30 * * * *")
    public void sendTasks () {
        telegramBot.sendTasks();
    }

    @Scheduled(cron = "0 */20 * * * *")
    public void homeWorkRemind () {
        telegramBot.homeWorkRemind();
    }
}
