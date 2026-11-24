package com.learn.assistant.controller;

import java.util.List;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
public class MemoryController {

    @Qualifier ("chatMemory")
    private final ChatMemory chatMemory;


    @GetMapping ("/memory")
    public List<String> view(@RequestParam String conversationId) {
        return chatMemory.get(conversationId).stream()
                .map(m -> m.getMessageType() + ": " + m.getText())
                .toList();
    }

    @DeleteMapping ("/memory")
    public String clear(@RequestParam String conversationId) {
        chatMemory.clear(conversationId);
        return "Cleared " + conversationId;
    }
}