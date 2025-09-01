package com.smooth.driving_analysis_service.driving.entity;

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

    @Column(nullable = false, unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    private DrivingCharacterType characterType;

    private String characterTrait;

    private String description;

    private int analyzedDistanceKm;

    private Long fromRecordId;

    private Long toRecordId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
