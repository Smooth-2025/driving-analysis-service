package com.smooth.driving_analysis_service.batch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "batch_watermark")
public class BatchWatermark {

    @Id
    private Long id;  // 고정 1로 사용 (싱글턴)

    @Column(name = "last_processed_date")
    private LocalDate lastProcessedDate;
}
