package com.smooth.driving_analysis_service.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@EnableScheduling
@Slf4j
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("DrivingAnalysis-");
        executor.initialize();
        return executor;
    }

    // 모니터링 스케줄러 추가
    @Scheduled(fixedRate = 30000) // 30초마다
    public void logThreadPoolStatus(@Qualifier("taskExecutor") ThreadPoolTaskExecutor taskExecutor) {
        ThreadPoolExecutor threadPool = taskExecutor.getThreadPoolExecutor();

        log.info("ThreadPool Status - Active: {}, Pool: {}, Queue: {}, Completed: {}",
                threadPool.getActiveCount(),
                threadPool.getPoolSize(),
                threadPool.getQueue().size(),
                threadPool.getCompletedTaskCount());
    }
}
