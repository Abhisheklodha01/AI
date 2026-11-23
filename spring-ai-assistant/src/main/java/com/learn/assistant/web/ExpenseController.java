package com.learn.assistant.web;

import com.learn.assistant.model.Expense;
import com.learn.assistant.model.ExpenseList;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
public class ExpenseController {

    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

    private final ChatClient expenseParser;

    public ExpenseController(@Qualifier("expenseParser") ChatClient expenseParser) {
        this.expenseParser = expenseParser;
    }

    @GetMapping("/expenses/parse")
    public ExpenseList parse(@RequestParam String text) {
        // 1. Ask the LLM to extract expenses
        ExpenseList result = expenseParser.prompt()
                .user(text)
                .call()
                .entity(ExpenseList.class);

        // 2. Verify what the LLM returned
        LocalDate today = LocalDate.now(ZONE);
        List<Expense> valid = result.expenses().stream()
                .filter(e -> e.amount() != null && e.amount().signum() > 0)
                .filter(e -> e.date() != null && !e.date().isAfter(today))
                .toList();

        // 3. Return only the valid ones
        return new ExpenseList(valid);
    }
}