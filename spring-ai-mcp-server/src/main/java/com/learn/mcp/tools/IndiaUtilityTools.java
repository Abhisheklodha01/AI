package com.learn.mcp.tools;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Component
public class IndiaUtilityTools {

    // Fixed example rates (rupees per 1 unit). A real server would call a rates API.
    private static final Map<String, BigDecimal> INR_PER_UNIT = Map.of(
            "USD", new BigDecimal("84.00"),
            "EUR", new BigDecimal("91.00"),
            "GBP", new BigDecimal("107.00"),
            "AED", new BigDecimal("22.90"));

    @McpTool(name = "convert_to_inr",
             description = "Convert an amount in USD, EUR, GBP or AED to Indian rupees (example fixed rates)")
    public String convertToInr(
            @McpToolParam(description = "Amount to convert", required = true) BigDecimal amount,
            @McpToolParam(description = "Currency code: USD, EUR, GBP or AED", required = true) String currency) {
        BigDecimal rate = INR_PER_UNIT.get(currency.toUpperCase());
        if (rate == null) {
            throw new IllegalArgumentException("Unsupported currency: " + currency + ". Use USD, EUR, GBP or AED.");
        }
        BigDecimal inr = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        return amount + " " + currency.toUpperCase() + " = ₹" + inr + " (rate " + rate + ")";
    }

    @McpTool(name = "calculate_gst",
             description = "Calculate GST on a price in rupees. Returns the GST amount and the total.")
    public String calculateGst(
            @McpToolParam(description = "Price before GST, in rupees", required = true) BigDecimal price,
            @McpToolParam(description = "GST rate in percent: 5, 12, 18 or 28", required = true) int ratePercent) {
        BigDecimal gst = price.multiply(BigDecimal.valueOf(ratePercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return "Price ₹" + price + " + " + ratePercent + "% GST (₹" + gst + ") = ₹" + price.add(gst);
    }
}