package com.learn.workflow.dto;

import com.learn.workflow.enums.SupportCategory;

public record SupportResponse(SupportCategory routedTo,
        String routingReason,
        String reply,
        long timeTakenMs) {
}