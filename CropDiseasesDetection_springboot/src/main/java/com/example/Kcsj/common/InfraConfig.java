package com.example.Kcsj.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;

@Configuration
public class InfraConfig {
    @Value("${app.task.core-pool-size:4}")
    private int corePoolSize;

    @Value("${app.task.max-pool-size:8}")
    private int maxPoolSize;

    @Value("${app.task.queue-capacity:200}")
    private int queueCapacity;

    @Value("${app.task.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("async-task-");
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.initialize();
        return executor;
    }
}

