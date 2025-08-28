package com.smooth.driving_analysis_service.driving.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DrivingCompletionRequestDto {

    @NotBlank(message = "주행 ID는 필수입니다.")
    private String drivingId;

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;
}
