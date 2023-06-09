package com.max.team.splitter.bot;

import com.max.team.splitter.bot.handlers.CompositeUpdateHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TeamSplitterBot {
    private static final Logger log = LoggerFactory.getLogger(TeamSplitterBot.class);

    @Autowired
    private TelegramBot bot;
    @Autowired
    private CompositeUpdateHandler updateHandler;


    public void start() {
        log.info("Starting TeamSplitter Bot");
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                log.info("Received update={}", update);
                updateHandler.handle(update);
            }

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
        log.info("Started Team Splitter Bot");
    }

    public void stop() {
        log.info("Stopping TeamSplitter Bot");
        bot.shutdown();
        log.info("Stopped TeamSplitter Bot");
    }
}
