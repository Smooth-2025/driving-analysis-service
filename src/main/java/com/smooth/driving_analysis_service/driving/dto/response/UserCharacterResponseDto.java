package com.smooth.driving_analysis_service.driving.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCharacterResponseDto {
    private Long userId;
    private String character;

    public static UserCharacterResponseDto of(Long userId, String character) {
        return  new UserCharacterResponseDto(userId, character);
    }
}


