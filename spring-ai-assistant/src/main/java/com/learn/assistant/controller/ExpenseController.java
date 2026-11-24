package com.learn.assistant.controller;

import com.learn.assistant.dto.ExpenseDto;
import com.learn.assistant.service.ExpenseService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor 
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping("/parse")
    public List<ExpenseDto> parse(@RequestParam String text) {
        return expenseService.parse(text);
    }

    @PostMapping
    public List<ExpenseDto> add(@RequestParam String userId, @RequestBody String text) {
        return expenseService.parseAndSave(userId, text);
    }

    @GetMapping
    public List<ExpenseDto> list(@RequestParam String userId) {
        return expenseService.findAll(userId);
    }
}