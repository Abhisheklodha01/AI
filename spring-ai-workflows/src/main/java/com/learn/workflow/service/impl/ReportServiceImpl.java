package com.learn.workflow.service.impl;

import com.learn.workflow.dto.ReportPlan;
import com.learn.workflow.dto.ReportRequest;
import com.learn.workflow.dto.ReportResponse;
import com.learn.workflow.dto.SectionResult;
import com.learn.workflow.dto.SubTask;
import com.learn.workflow.service.ReportService;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final int MAX_SUBTASKS = 4;

    private final ChatClient chatClient;
    private final ExecutorService aiExecutor;

    @Value("classpath:prompts/report-orchestrator.st")
    private Resource orchestratorPrompt;

    @Value("classpath:prompts/report-worker.st")
    private Resource workerPrompt;

    @Value("classpath:prompts/report-synthesizer.st")
    private Resource synthesizerPrompt;

    @Override
    public ReportResponse write(ReportRequest request) {
        long start = System.currentTimeMillis();
        String question = request.question();

        ReportPlan plan = makePlan(question);
        List<SectionResult> sections = runWorkers(question, plan.subTasks());
        String finalReport = synthesize(question, plan.title(), sections);

        long timeTaken = System.currentTimeMillis() - start;
        log.info("Report finished in {} ms", timeTaken);
        return new ReportResponse(plan, sections, finalReport, timeTaken);
    }

    // ---------- Step 1: orchestrator plans the subtasks ----------
    private ReportPlan makePlan(String question) {
        log.info("Orchestrator: planning '{}'", question);
        ReportPlan plan = chatClient.prompt()
                .system(orchestratorPrompt)
                .user(question)
                .call()
                .entity(ReportPlan.class);

        if (plan == null || plan.subTasks() == null || plan.subTasks().isEmpty()) {
            throw new IllegalStateException("Could not plan this report");
        }

        // Safety limit: never run more workers than MAX_SUBTASKS
        List<SubTask> subTasks = plan.subTasks().stream().limit(MAX_SUBTASKS).toList();
        log.info("Plan '{}': {}", plan.title(), subTasks.stream().map(SubTask::name).toList());
        return new ReportPlan(plan.title(), subTasks);
    }

    // ---------- Step 2: workers run in parallel ----------
    private List<SectionResult> runWorkers(String question, List<SubTask> subTasks) {
        // Start ALL workers first...
        List<CompletableFuture<SectionResult>> futures = subTasks.stream()
                .map(task -> CompletableFuture.supplyAsync(() -> runWorker(question, task), aiExecutor))
                .toList();

        // ...then wait for all of them
        return futures.stream().map(this::await).toList();
    }

    private SectionResult runWorker(String question, SubTask task) {
        log.info("Worker started: {}", task.name());
        String content = chatClient.prompt()
                .system(s -> s.text(workerPrompt)
                        .param("question", question)
                        .param("name", task.name())
                        .param("instructions", task.instructions()))
                .user("Write your part.")
                .call()
                .content();
        log.info("Worker finished: {}", task.name());
        return new SectionResult(task.name(), content);
    }

    // ---------- Step 3: synthesizer combines everything ----------
    private String synthesize(String question, String title, List<SectionResult> sections) {
        log.info("Synthesizer: combining {} sections", sections.size());
        String sectionsText = sections.stream()
                .map(s -> "## " + s.name() + "\n" + s.content())
                .collect(Collectors.joining("\n\n"));

        return chatClient.prompt()
                .system(s -> s.text(synthesizerPrompt)
                        .param("title", title)
                        .param("question", question)
                        .param("sections", sectionsText))
                .user("Write the final report.")
                .call()
                .content();
    }

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