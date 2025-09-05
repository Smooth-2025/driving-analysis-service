package com.smooth.driving_analysis_service.driving.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCharacterBulkResponseDto {

    private List<UserCharacterResponseDto> data;

    @JsonProperty("generatedAtUtc")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant generatedAtUtc;



    public static UserCharacterBulkResponseDto of(List<UserCharacterResponseDto> data) {
        return UserCharacterBulkResponseDto.builder()
                .data(data)
                .generatedAtUtc(Instant.now())
                .build();
    }
}
