package com.learn.assistant.dto;

import java.util.List;

public record ExpenseListDto(List<ExpenseDto> expenses) {
    public ExpenseListDto {
        expenses = (expenses == null) ? List.of() : expenses;
    }
}