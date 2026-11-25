package com.learn.workflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {

    @Bean(destroyMethod = "close")
    public ExecutorService aiExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}