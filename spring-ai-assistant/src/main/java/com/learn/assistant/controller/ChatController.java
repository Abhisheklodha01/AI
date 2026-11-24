package com.learn.assistant.controller;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learn.assistant.tools.ExpenseTools;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {

    @Qualifier("assistantAgent")
    private final ChatClient chatClient;

    @GetMapping("/chat")
    public String chat(@RequestParam String message,
            @RequestParam String conversationId) {
        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .toolContext(Map.of(ExpenseTools.USER_ID, conversationId))
                .call()
                .content();
    }
}