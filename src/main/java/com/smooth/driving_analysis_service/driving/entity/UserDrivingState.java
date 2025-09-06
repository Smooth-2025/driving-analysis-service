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

    @Builder.Default
    private double pendingDistanceKm = 0.0;

    @Builder.Default
    private double totalDistanceKm = 0.0;

    private Long lastAnalyzedRecordId;

    @Enumerated(EnumType.STRING)
    private DrivingCharacterType currentCharacterType;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void update(double drivingDistanceKm) {
        this.pendingDistanceKm += drivingDistanceKm;
        this.totalDistanceKm += drivingDistanceKm;
    }

    public void reset(Long lastAnalyzedRecordId, DrivingCharacterType currentCharacterType) {
        this.pendingDistanceKm = 0.0;
        this.lastAnalyzedRecordId = lastAnalyzedRecordId;
        this.currentCharacterType = currentCharacterType;
    }

    public static UserDrivingState createInitialUserDrivingState(Long userId) {
        return UserDrivingState.builder()
                .userId(userId)
                .lastAnalyzedRecordId(null)
                .currentCharacterType(DrivingCharacterType.NONE)
                .build();
    }
}
