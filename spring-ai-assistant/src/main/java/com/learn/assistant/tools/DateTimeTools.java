package com.learn.assistant.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

@Component
public class DateTimeTools {

    private static final Logger log = LoggerFactory.getLogger(DateTimeTools.class);
    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

    @Tool(description = "Get the current date, time and day of the week in the user's timezone (Asia/Kolkata)")
    public String getCurrentDateTime() {
        String now = ZonedDateTime.now(ZONE)
                .format(DateTimeFormatter.ofPattern("EEEE, yyyy-MM-dd HH:mm z"));
        log.info("Tool called: getCurrentDateTime() -> {}", now);
        return now;
    }

    @Tool(description = "Count the number of days from one date to another. Dates must be in yyyy-MM-dd format.")
    public long daysBetween(
            @ToolParam(description = "Start date in yyyy-MM-dd format") String from,
            @ToolParam(description = "End date in yyyy-MM-dd format") String to) {
        log.info("Tool called: daysBetween({}, {})", from, to);
        return ChronoUnit.DAYS.between(parse(from), parse(to));
    }

    @Tool(description = "Add (or subtract, using a negative number) days to a date and return the resulting date with its day of week")
    public String addDays(
            @ToolParam(description = "Date in yyyy-MM-dd format") String date,
            @ToolParam(description = "Number of days to add; negative to go back in time") long days) {
        log.info("Tool called: addDays({}, {})", date, days);
        LocalDate result = parse(date).plusDays(days);
        return result + " (" + result.getDayOfWeek() + ")";
    }

    private LocalDate parse(String date) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            log.warn("Invalid date from model: {}", date);
            throw new IllegalArgumentException(
                    "Invalid date '" + date + "'. Use yyyy-MM-dd format, for example 2026-01-26.");
        }
    }
}