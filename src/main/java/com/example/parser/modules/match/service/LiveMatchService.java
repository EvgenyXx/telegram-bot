package com.example.parser.modules.match.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LiveMatchService {

    private final Map<Long, String> userLinks = new HashMap<>();
    private final Map<Long, Boolean> waitingForLink = new HashMap<>();
    private final Map<Long, Integer> messageIds = new HashMap<>();
    private final Map<Long, Boolean> autoUpdate = new HashMap<>();
    private final Map<Long, String> lastMessages = new HashMap<>();

    public void startWaiting(Long chatId) {
        waitingForLink.put(chatId, true);
    }

    public boolean isWaiting(Long chatId) {
        return waitingForLink.getOrDefault(chatId, false);
    }

    public void setLink(Long chatId, String url) {
        userLinks.put(chatId, url);
        waitingForLink.remove(chatId);
    }

    public String getLink(Long chatId) {
        return userLinks.get(chatId);
    }

    public void clear(Long chatId) {
        userLinks.remove(chatId);
    }

    public void setMessageId(Long chatId, Integer messageId) {
        messageIds.put(chatId, messageId);
    }

    public Integer getMessageId(Long chatId) {
        return messageIds.get(chatId);
    }

    public void clearMessageId(Long chatId) {
        messageIds.remove(chatId);
    }

    public void startAutoUpdate(Long chatId) {
        autoUpdate.put(chatId, true);
    }

    public void stopAutoUpdate(Long chatId) {
        autoUpdate.remove(chatId);
    }

    public boolean isAutoUpdating(Long chatId) {
        return autoUpdate.getOrDefault(chatId, false);
    }

    public void stopWaiting(Long chatId) {
        waitingForLink.remove(chatId);
    }

    public void setLastMessage(Long chatId, String text) {
        lastMessages.put(chatId, text);
    }

    public String getLastMessage(Long chatId) {
        return lastMessages.get(chatId);
    }

    public void clearLastMessage(Long chatId) {
        lastMessages.remove(chatId);
    }
}