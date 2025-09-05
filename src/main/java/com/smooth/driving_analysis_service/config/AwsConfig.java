package com.smooth.driving_analysis_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class AwsConfig {
    // AthenaClient는 AthenaConfig에서 정의하므로 여기서는 제거
}