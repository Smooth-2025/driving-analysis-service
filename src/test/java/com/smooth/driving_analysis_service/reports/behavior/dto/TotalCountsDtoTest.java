package com.smooth.driving_analysis_service.reports.behavior.dto;

import com.smooth.driving_analysis_service.reports.behavior.dto.response.TotalCountsDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TotalCountsDto 단위 테스트")
class TotalCountsDtoTest {

    @Test
    @DisplayName("정상적인 데이터로 DTO 생성")
    void createTotalCountsDto_Success() {
        // given
        int hardBrake = 38;
        int rapidAccel = 42;
        int laneChange = 17;

        // when
        TotalCountsDto dto = TotalCountsDto.of(hardBrake, rapidAccel, laneChange);

        // then
        assertThat(dto.getHardBrake()).isEqualTo(38);
        assertThat(dto.getRapidAccel()).isEqualTo(42);
        assertThat(dto.getLaneChange()).isEqualTo(17);
        assertThat(dto.getTotal()).isEqualTo(97);
    }

    @Test
    @DisplayName("0 값으로 DTO 생성")
    void createTotalCountsDto_ZeroValues() {
        // when
        TotalCountsDto dto = TotalCountsDto.of(0, 0, 0);

        // then
        assertThat(dto.getHardBrake()).isEqualTo(0);
        assertThat(dto.getRapidAccel()).isEqualTo(0);
        assertThat(dto.getLaneChange()).isEqualTo(0);
        assertThat(dto.getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("큰 숫자로 DTO 생성")
    void createTotalCountsDto_LargeNumbers() {
        // when
        TotalCountsDto dto = TotalCountsDto.of(1000, 2000, 500);

        // then
        assertThat(dto.getTotal()).isEqualTo(3500);
    }

    @Test
    @DisplayName("기본 생성자로 DTO 생성")
    void createTotalCountsDto_DefaultConstructor() {
        // when
        TotalCountsDto dto = new TotalCountsDto();
        dto.setHardBrake(10);
        dto.setRapidAccel(20);
        dto.setLaneChange(5);

        // then
        assertThat(dto.getTotal()).isEqualTo(35);
    }
}