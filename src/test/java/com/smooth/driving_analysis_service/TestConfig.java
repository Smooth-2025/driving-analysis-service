package com.smooth.driving_analysis_service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.athena.AthenaClient;
import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    public AthenaClient athenaClient() {
        return mock(AthenaClient.class);
    }
}
