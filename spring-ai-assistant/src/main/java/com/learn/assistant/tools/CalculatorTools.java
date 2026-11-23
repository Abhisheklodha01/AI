package com.learn.assistant.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;

@Component
public class CalculatorTools {

    private static final Logger logger = LoggerFactory.getLogger(CalculatorTools.class);

    @Tool(description = "Add two numbers and return the sum")
    public BigDecimal add(
            @ToolParam(description = "The first number") BigDecimal a,
            @ToolParam(description = "The second number") BigDecimal b) {
        logger.info("Tool called: add({}, {})", a, b);
        return a.add(b);
    }

    @Tool(description = "Subtract the second number from the first number (a - b)")
    public BigDecimal subtract(
            @ToolParam(description = "The number to subtract from") BigDecimal a,
            @ToolParam(description = "The number to subtract") BigDecimal b) {
        logger.info("Tool called: subtract({}, {})", a, b);
        return a.subtract(b);
    }

    @Tool(description = "Multiply two numbers and return the product")
    public BigDecimal multiply(
            @ToolParam(description = "The first number") BigDecimal a,
            @ToolParam(description = "The second number") BigDecimal b) {
        logger.info("Tool called: multiply({}, {})", a, b);
        return a.multiply(b);
    }

    @Tool(description = "Divide the first number by the second number (a / b)")
    public BigDecimal divide(
            @ToolParam(description = "The dividend, the number being divided") BigDecimal a,
            @ToolParam(description = "The divisor, must not be zero") BigDecimal b) {
        logger.info("Tool called: divide({}, {})", a, b);
        if (b.signum() == 0) {
            logger.warn("Division by zero attempted: {} / {}", a, b);
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        return a.divide(b, MathContext.DECIMAL64);
    }
}