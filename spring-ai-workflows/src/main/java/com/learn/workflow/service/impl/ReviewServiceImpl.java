package com.learn.workflow.service.impl;

import com.learn.workflow.dto.IssueList;
import com.learn.workflow.dto.ReviewAnalysisResponse;
import com.learn.workflow.dto.ReviewRequest;
import com.learn.workflow.dto.SentimentResult;
import com.learn.workflow.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ChatClient chatClient;
    private final ExecutorService aiExecutor;

    @Value("classpath:prompts/review-sentiment.st")
    private Resource sentimentPrompt;

    @Value("classpath:prompts/review-issues.st")
    private Resource issuesPrompt;

    @Value("classpath:prompts/review-reply.st")
    private Resource replyPrompt;

    /** A task's result plus how long it took. */
    private record Timed<T>(T value, long ms) {}

    @Override
    public ReviewAnalysisResponse analyze(ReviewRequest request) {
        String review = request.review();
        long start = System.currentTimeMillis();

        // Start all three tasks at the same time
        CompletableFuture<Timed<SentimentResult>> sentimentTask =
                runAsync("sentiment", () -> analyzeSentiment(review));
        CompletableFuture<Timed<IssueList>> issuesTask =
                runAsync("issues", () -> findIssues(review));
        CompletableFuture<Timed<String>> replyTask =
                runAsync("reply", () -> writeReply(review));

        // Wait until all three are done
        Timed<SentimentResult> sentiment = await(sentimentTask);
        Timed<IssueList> issues = await(issuesTask);
        Timed<String> reply = await(replyTask);

        long parallelTime = System.currentTimeMillis() - start;
        long sequentialTime = sentiment.ms() + issues.ms() + reply.ms();
        log.info("Parallel: {} ms, would be {} ms one after another", parallelTime, sequentialTime);

        List<String> issueList = issues.value() == null || issues.value().issues() == null
                ? List.of()
                : issues.value().issues();

        return new ReviewAnalysisResponse(sentiment.value(), issueList, reply.value(),
                parallelTime, sequentialTime);
    }

    // ---------- the three independent tasks ----------

    private SentimentResult analyzeSentiment(String review) {
        return chatClient.prompt().system(sentimentPrompt).user(review)
                .call().entity(SentimentResult.class);
    }

    private IssueList findIssues(String review) {
        return chatClient.prompt().system(issuesPrompt).user(review)
                .call().entity(IssueList.class);
    }

    private String writeReply(String review) {
        return chatClient.prompt().system(replyPrompt).user(review)
                .call().content();
    }

    // ---------- helpers for running in parallel ----------

    /** Run a task on the executor and measure how long it takes. */
    private <T> CompletableFuture<Timed<T>> runAsync(String name, Supplier<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            long t = System.currentTimeMillis();
            T result = task.get();
            long ms = System.currentTimeMillis() - t;
            log.info("Task '{}' finished in {} ms", name, ms);
            return new Timed<>(result, ms);
        }, aiExecutor);
    }

    /** Wait for a task. If it failed, rethrow the real error (not a wrapper). */
    private <T> T await(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw e;
        }
    }
}