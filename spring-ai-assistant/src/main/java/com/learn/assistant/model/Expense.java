package com.learn.assistant.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Expense(String item, BigDecimal amount, Category category, LocalDate date) {}