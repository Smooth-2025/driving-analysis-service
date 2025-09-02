package com.smooth.driving_analysis_service.trigger.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "redis.stream")
@Data
@Component
public class TriggerStreamProperties {
    private Driving driving = new Driving();
    private Report report = new Report();

    @Data
    public static class Driving {
        private String name;    // application.yml의 name
        private String group;   // application.yml의 group
        private String consumer; // application.yml의 consumer

        // getter 메서드명 확인 필요
        public String getStream() {
            return name;  // name을 stream으로 매핑
        }
    }

    @Data
    public static class Report {
        private String name;
        private String group;
        private String consumer;
        public String getStream() {  // ← 이 메서드 추가
            return name;  // name을 stream으로 매핑
        }
    }
}