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
public class DrivingCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    private Double currentDistance;

    @Enumerated(EnumType.STRING)
    private DrivingCharacterType characterType;

    private String characterTrait;

    private String description;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
