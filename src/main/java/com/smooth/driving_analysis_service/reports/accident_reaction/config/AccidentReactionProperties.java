package com.smooth.driving_analysis_service.reports.accident_reaction.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "accident.reaction")
public class AccidentReactionProperties {
    
    private Resolve resolve = new Resolve();
    private Analysis analysis = new Analysis();
    private Athena athena = new Athena();
    
    @Getter
    @Setter
    public static class Resolve {
        private int bufferSec = 300; // drivingId 탐색 버퍼 (기본 5분)
    }
    
    @Getter
    @Setter
    public static class Analysis {
        private int windowSec = 120; // 반응 탐색 윈도우 (기본 2분)
    }
    
    @Getter
    @Setter
    public static class Athena {
        private String region = "ap-northeast-2";
        private String database = "driving_analytics";
        private String table = "driving_events";
        private String output = "s3://smooth-athena-query-results/accident-reaction/";
        private String workgroup = "primary";
    }
}