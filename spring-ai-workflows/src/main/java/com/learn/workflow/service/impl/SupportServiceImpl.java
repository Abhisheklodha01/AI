package com.learn.workflow.service.impl;

import com.learn.workflow.dto.RouteDecision;
import com.learn.workflow.dto.SupportRequest;
import com.learn.workflow.dto.SupportResponse;
import com.learn.workflow.enums.SupportCategory;
import com.learn.workflow.service.SupportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupportServiceImpl implements SupportService {

    private final ChatClient chatClient;

    @Value("classpath:prompts/support-router.st")
    private Resource routerPrompt;

    @Value("classpath:prompts/support-billing.st")
    private Resource billingPrompt;

    @Value("classpath:prompts/support-technical.st")
    private Resource technicalPrompt;

    @Value("classpath:prompts/support-general.st")
    private Resource generalPrompt;

    @Override
    public SupportResponse handle(SupportRequest request) {
        long start = System.currentTimeMillis();

        RouteDecision decision = route(request.message());
        String reply = answer(decision.category(), request.message());

        long timeTaken = System.currentTimeMillis() - start;
        return new SupportResponse(decision.category(), decision.reason(), reply, timeTaken);
    }

    // ---------- Step 1: decide which specialist ----------
    private RouteDecision route(String message) {
        RouteDecision decision = chatClient.prompt()
                .system(routerPrompt)
                .user(message)
                .call()
                .entity(RouteDecision.class);

        // Safety net: if the router fails, send to GENERAL instead of crashing
        if (decision == null || decision.category() == null) {
            log.warn("Router gave no category, falling back to GENERAL");
            return new RouteDecision(SupportCategory.GENERAL, "Router could not decide");
        }
        log.info("Routed to {}: {}", decision.category(), decision.reason());
        return decision;
    }

    // ---------- Step 2: the chosen specialist answers ----------
    private String answer(SupportCategory category, String message) {
        Resource specialistPrompt = switch (category) {
            case BILLING -> billingPrompt;
            case TECHNICAL -> technicalPrompt;
            case GENERAL -> generalPrompt;
        };

        return chatClient.prompt()
                .system(specialistPrompt)
                .user(message)
                .call()
                .content();
    }
}