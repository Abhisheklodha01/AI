package com.learn.workflow.service;

import com.learn.workflow.dto.SupportRequest;
import com.learn.workflow.dto.SupportResponse;

public interface SupportService {
    SupportResponse handle(SupportRequest request);
}