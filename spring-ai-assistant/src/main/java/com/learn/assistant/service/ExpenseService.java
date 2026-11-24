package com.learn.assistant.service;

import com.learn.assistant.dto.ExpenseDto;
import com.learn.assistant.enums.Category;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ExpenseService {

    List<ExpenseDto> parse(String text);

    List<ExpenseDto> parseAndSave(String userId, String text);

    List<ExpenseDto> findAll(String userId);

    List<ExpenseDto> findBetween(String userId, LocalDate from, LocalDate to, Category category);

    BigDecimal totalBetween(String userId, LocalDate from, LocalDate to, Category category);

    Map<Category, BigDecimal> totalByCategory(String userId, LocalDate from, LocalDate to);

    ExpenseDto add(String userId, ExpenseDto expense);
}