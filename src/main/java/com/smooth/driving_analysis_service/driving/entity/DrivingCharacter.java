package com.smooth.driving_analysis_service.driving.entity;

import com.smooth.driving_analysis_service.driving.dto.result.DrivingCharacterAnalysisResultDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DrivingCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    private DrivingCharacterType characterType;

    private int confidenceScore;

    private String characterTrait;

    private String drivingStyle;

    private String improvementSuggestions;

    private String speedPreference;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    private int analyzedDistanceKm;

    private Long fromRecordId;

    private Long toRecordId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public void update(DrivingCharacterAnalysisResultDto result, Long toRecordId, int analyzedDistanceKm) {
        this.characterType = DrivingCharacterType.valueOf(result.getCharacterType());
        this.confidenceScore = result.getConfidenceScore();
        this.characterTrait = result.getCharacterTrait();
        this.drivingStyle = result.getDrivingStyle();
        this.improvementSuggestions = result.getImprovementSuggestions();
        this.speedPreference = result.getSpeedPreference();
        this.description = result.getPersonalityDescription();
        this.toRecordId = toRecordId;
        this.analyzedDistanceKm = analyzedDistanceKm;
    }

    public static DrivingCharacter createInitialDrivingCharacter(Long userId, Long fromRecordId) {
        return DrivingCharacter.builder()
                .userId(userId)
                .characterType(DrivingCharacterType.NONE)
                .fromRecordId(fromRecordId)
                .build();
    }

    public static DrivingCharacter createNoneDrivingCharacter() {
        return DrivingCharacter.builder()
                .characterType(DrivingCharacterType.NONE)
                .build();
    }
}
