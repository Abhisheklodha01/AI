package com.learn.assistant.model;

import java.util.List;

public record ExpenseList(List<Expense> expenses) {
    public ExpenseList {
        expenses = (expenses == null) ? List.of() : expenses;
    }
}