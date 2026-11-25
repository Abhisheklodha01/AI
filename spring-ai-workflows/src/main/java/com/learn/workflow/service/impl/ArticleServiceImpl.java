package com.learn.workflow.service.impl;

import com.learn.workflow.dto.ArticleBrief;
import com.learn.workflow.dto.ArticleOutline;
import com.learn.workflow.dto.ArticleRequest;
import com.learn.workflow.dto.ArticleResponse;
import com.learn.workflow.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ChatClient chatClient;

    @Value("classpath:prompts/article-brief.st")
    private Resource briefPrompt;

    @Value("classpath:prompts/article-outline.st")
    private Resource outlinePrompt;

    @Value("classpath:prompts/article-draft.st")
    private Resource draftPrompt;

    @Value("classpath:prompts/article-polish.st")
    private Resource polishPrompt;

    @Override
    public ArticleResponse write(ArticleRequest request) {
        long start = System.currentTimeMillis();

        ArticleBrief brief = extractBrief(request.prompt());
        ArticleOutline outline = createOutline(brief);
        String draft = writeDraft(outline);
        String finalArticle = polish(draft);

        long timeTaken = System.currentTimeMillis() - start;
        log.info("Chain finished in {} ms", timeTaken);
        return new ArticleResponse(brief, outline, draft, finalArticle, timeTaken);
    }

    // ---------- Step 0: free text → brief ----------
    private ArticleBrief extractBrief(String prompt) {
        log.info("Step 0/3: understanding the request");
        ArticleBrief brief = chatClient.prompt()
                .system(briefPrompt)
                .user(prompt)
                .call()
                .entity(ArticleBrief.class);

        if (brief == null || brief.topic() == null || brief.topic().isBlank()) {
            throw new IllegalArgumentException("Could not understand what the article should be about");
        }
        log.info("Brief: topic='{}', audience='{}'", brief.topic(), brief.audience());
        return brief;
    }

    // ---------- Step 1: brief → outline ----------
    private ArticleOutline createOutline(ArticleBrief brief) {
        log.info("Step 1/3: creating outline");
        ArticleOutline outline = chatClient.prompt()
                .system(outlinePrompt)
                .user("Topic: " + brief.topic() + "\nAudience: " + brief.audience())
                .call()
                .entity(ArticleOutline.class);

        if (outline == null || outline.sections() == null || outline.sections().isEmpty()) {
            throw new IllegalStateException("Could not create an outline for this topic");
        }
        log.info("Outline: '{}' with {} sections", outline.title(), outline.sections().size());
        return outline;
    }

    // ---------- Step 2: outline → draft ----------
    private String writeDraft(ArticleOutline outline) {
        log.info("Step 2/3: writing draft");
        return chatClient.prompt()
                .system(s -> s.text(draftPrompt)
                        .param("title", outline.title())
                        .param("sections", String.join("\n", outline.sections())))
                .user("Write the article.")
                .call()
                .content();
    }

    // ---------- Step 3: draft → polished article ----------
    private String polish(String draft) {
        log.info("Step 3/3: polishing");
        return chatClient.prompt()
                .system(s -> s.text(polishPrompt).param("draft", draft))
                .user("Polish the article.")
                .call()
                .content();
    }
}