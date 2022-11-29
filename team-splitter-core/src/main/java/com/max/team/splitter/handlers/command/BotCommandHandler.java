package com.max.team.splitter.handlers.command;

import com.max.team.splitter.handlers.BotCommand;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.MessageEntity;

import java.util.Set;

public interface BotCommandHandler {

    void handle(Message message, String text, MessageEntity botCommandEntity);

    Set<BotCommand> supports();
}
