package com.learn.knowledge.exception;

import com.openai.errors.OpenAIException;
import com.openai.errors.RateLimitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", ex.getMessage());
    }

    @ExceptionHandler(RateLimitException.class)
    public ProblemDetail handleRateLimit(RateLimitException ex) {
        log.warn("AI rate limit hit: {}", ex.getMessage());
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "AI service busy",
                "The AI service is busy. Please try again in a moment.");
    }

    @ExceptionHandler(OpenAIException.class)
    public ProblemDetail handleAiError(OpenAIException ex) {
        log.error("AI call failed", ex);
        return problem(HttpStatus.BAD_GATEWAY, "AI service error",
                "The AI service could not process the request.");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error",
                "Something went wrong. Please try again.");
    }

    @ExceptionHandler(UnsupportedFileTypeException.class)
    public ProblemDetail handleUnsupportedType(UnsupportedFileTypeException ex) {
        return problem(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported file type", ex.getMessage());
    }

    @ExceptionHandler(NoTextExtractedException.class)
    public ProblemDetail handleNoText(NoTextExtractedException ex) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, "No text found", ex.getMessage());
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        return pd;
    }
}