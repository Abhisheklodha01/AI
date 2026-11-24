package com.learn.assistant.service.impl;

import com.learn.assistant.dto.ExpenseDto;
import com.learn.assistant.dto.ExpenseListDto;
import com.learn.assistant.entity.Expense;
import com.learn.assistant.enums.Category;
import com.learn.assistant.repository.ExpenseRepository;
import com.learn.assistant.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

    @Qualifier("expenseParser")
    private final ChatClient expenseParser;
    private final ExpenseRepository expenseRepository;

    @Override
    public List<ExpenseDto> parse(String text) {
        LocalDate today = LocalDate.now(ZONE);

        ExpenseListDto result = expenseParser.prompt()
                .system(s -> s.param("today", today.toString()))
                .user(text)
                .call()
                .entity(ExpenseListDto.class);

        List<ExpenseDto> valid = result.expenses().stream()
                .filter(e -> e.amount() != null && e.amount().signum() > 0)
                .filter(e -> e.date() != null && !e.date().isAfter(today))
                .toList();

        log.info("LLM returned {} expenses, {} valid", result.expenses().size(), valid.size());
        return valid;
    }

    @Override
    @Transactional
    public List<ExpenseDto> parseAndSave(String userId, String text) {
        List<ExpenseDto> expenses = parse(text);
        expenseRepository.saveAll(expenses.stream().map(dto -> toEntity(userId, dto)).toList());
        return expenses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseDto> findAll(String userId) {
        return expenseRepository.findByUserIdOrderByExpenseDateDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseDto> findBetween(String userId, LocalDate from, LocalDate to, Category category) {
        List<Expense> expenses = (category == null)
                ? expenseRepository.findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(userId, from, to)
                : expenseRepository.findByUserIdAndCategoryAndExpenseDateBetweenOrderByExpenseDateDesc(
                        userId, category, from, to);
        return expenses.stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal totalBetween(String userId, LocalDate from, LocalDate to, Category category) {
        return findBetween(userId, from, to, category).stream()
                .map(ExpenseDto::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Category, BigDecimal> totalByCategory(String userId, LocalDate from, LocalDate to) {
        return findBetween(userId, from, to, null).stream()
                .collect(Collectors.groupingBy(
                        ExpenseDto::category,
                        () -> new EnumMap<>(Category.class),
                        Collectors.reducing(BigDecimal.ZERO, ExpenseDto::amount, BigDecimal::add)));
    }

    @Override
    @Transactional
    public ExpenseDto add(String userId, ExpenseDto expense) {
        if (expense.amount() == null || expense.amount().signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (expense.date() == null || expense.date().isAfter(LocalDate.now(ZONE))) {
            throw new IllegalArgumentException("Date must be today or in the past");
        }
        expenseRepository.save(toEntity(userId, expense));
        return expense;
    }

    private Expense toEntity(String userId, ExpenseDto dto) {
        return Expense.builder()
                .userId(userId)
                .item(dto.item())
                .amount(dto.amount())
                .category(dto.category())
                .expenseDate(dto.date())
                .build();
    }

    private ExpenseDto toDto(Expense e) {
        return new ExpenseDto(e.getItem(), e.getAmount(), e.getCategory(), e.getExpenseDate());
    }
}