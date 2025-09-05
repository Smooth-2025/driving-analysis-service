package com.smooth.driving_analysis_service.driving.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UserDrivingState {

    @Id
    private Long userId;

    private double pendingDistanceKm = 0.0;

    private double totalDistanceKm = 0.0;

    private Long lastAnalyzedRecordId;

    @Enumerated(EnumType.STRING)
    private DrivingCharacterType currentCharacterType;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
