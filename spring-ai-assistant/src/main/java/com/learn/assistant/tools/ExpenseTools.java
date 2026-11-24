package com.learn.assistant.tools;

import com.learn.assistant.dto.ExpenseDto;
import com.learn.assistant.enums.Category;
import com.learn.assistant.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpenseTools {

    public static final String USER_ID = "userId";

    private final ExpenseService expenseService;

    @Tool(description = """
            List the user's expenses between two dates (inclusive), newest first.
            Optionally filter by one category.""")
    public List<ExpenseDto> getExpenses(
            @ToolParam(description = "Start date, yyyy-MM-dd") LocalDate from,
            @ToolParam(description = "End date, yyyy-MM-dd") LocalDate to,
            @ToolParam(description = "Category to filter by; omit for all categories", required = false) Category category,
            ToolContext toolContext) {
        String userId = userId(toolContext);
        log.info("Tool called: getExpenses(user={}, {} → {}, category={})", userId, from, to, category);
        return expenseService.findBetween(userId, from, to, category);
    }

    @Tool(description = """
            Total amount the user spent between two dates (inclusive), in rupees.
            Optionally for one category only.""")
    public BigDecimal getTotalSpent(
            @ToolParam(description = "Start date, yyyy-MM-dd") LocalDate from,
            @ToolParam(description = "End date, yyyy-MM-dd") LocalDate to,
            @ToolParam(description = "Category; omit for all categories", required = false) Category category,
            ToolContext toolContext) {
        String userId = userId(toolContext);
        log.info("Tool called: getTotalSpent(user={}, {} → {}, category={})", userId, from, to, category);
        return expenseService.totalBetween(userId, from, to, category);
    }

    @Tool(description = "Total spent per category between two dates (inclusive), in rupees")
    public Map<Category, BigDecimal> getSpendingByCategory(
            @ToolParam(description = "Start date, yyyy-MM-dd") LocalDate from,
            @ToolParam(description = "End date, yyyy-MM-dd") LocalDate to,
            ToolContext toolContext) {
        String userId = userId(toolContext);
        log.info("Tool called: getSpendingByCategory(user={}, {} → {})", userId, from, to);
        return expenseService.totalByCategory(userId, from, to);
    }

    @Tool(description = "Record a new expense for the user")
    public String addExpense(
            @ToolParam(description = "What the money was spent on, e.g. 'tea'") String item,
            @ToolParam(description = "Amount in rupees, greater than zero") BigDecimal amount,
            @ToolParam(description = "Expense category") Category category,
            @ToolParam(description = "Date of the expense, yyyy-MM-dd") LocalDate date,
            ToolContext toolContext) {
        String userId = userId(toolContext);
        log.info("Tool called: addExpense(user={}, {}, {}, {}, {})", userId, item, amount, category, date);
        expenseService.add(userId, new ExpenseDto(item, amount, category, date));
        return "Saved: " + item + " ₹" + amount + " (" + category + ") on " + date;
    }

    private String userId(ToolContext toolContext) {
        Object userId = toolContext.getContext().get(USER_ID);
        if (userId == null) {
            throw new IllegalStateException("No user in tool context");
        }
        return userId.toString();
    }
}