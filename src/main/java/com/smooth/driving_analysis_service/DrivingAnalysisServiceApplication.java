package com.smooth.driving_analysis_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DrivingAnalysisServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DrivingAnalysisServiceApplication.class, args);
	}

}
