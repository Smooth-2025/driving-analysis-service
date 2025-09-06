package com.smooth.driving_analysis_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "aws.region=us-east-1",
    "aws.s3.bucket=test-bucket",
    "spring.redis.host=localhost",
    "spring.redis.port=6379"
})
class DrivingAnalysisServiceApplicationTests {

	@Test
	void contextLoads() {
		// Spring Context 로딩 테스트
	}

}
