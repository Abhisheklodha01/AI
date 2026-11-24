package com.learn.assistant.dto;

import com.learn.assistant.enums.Category;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseDto(String item, BigDecimal amount, Category category, LocalDate date) {}