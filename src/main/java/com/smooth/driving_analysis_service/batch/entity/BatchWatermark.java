package com.smooth.driving_analysis_service.batch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "batch_watermark")
@Getter @Setter
public class BatchWatermark {

    @Id
    private Integer id = 1;

    @Column(name = "last_processed_date")
    private LocalDate lastProcessedDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
