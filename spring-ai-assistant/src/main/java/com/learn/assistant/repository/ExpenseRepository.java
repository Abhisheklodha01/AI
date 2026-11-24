package com.learn.assistant.repository;

import com.learn.assistant.entity.Expense;
import com.learn.assistant.enums.Category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserIdOrderByExpenseDateDesc(String userId);

    List<Expense> findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(
            String userId, LocalDate from, LocalDate to);

    List<Expense> findByUserIdAndCategoryAndExpenseDateBetweenOrderByExpenseDateDesc(
            String userId, Category category, LocalDate from, LocalDate to);
}