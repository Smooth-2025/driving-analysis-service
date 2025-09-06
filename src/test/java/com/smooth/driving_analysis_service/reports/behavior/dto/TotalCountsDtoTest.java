package com.smooth.driving_analysis_service.reports.behavior.dto;

import com.smooth.driving_analysis_service.reports.behavior.dto.response.TotalCountsDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TotalCountsDto 단위 테스트")
class TotalCountsDtoTest {

    @Test
    @DisplayName("정상적인 값으로 생성")
    void createWithValidValues() {
        // when
        TotalCountsDto dto = TotalCountsDto.of(38, 42, 17);

        // then
        assertThat(dto.getHardBrake()).isEqualTo(38);
        assertThat(dto.getRapidAccel()).isEqualTo(42);
        assertThat(dto.getLaneChange()).isEqualTo(17);
        assertThat(dto.getTotal()).isEqualTo(97);
    }

    @Test
    @DisplayName("0값으로 생성")
    void createWithZeroValues() {
        // when
        TotalCountsDto dto = TotalCountsDto.of(0, 0, 0);

        // then
        assertThat(dto.getHardBrake()).isEqualTo(0);
        assertThat(dto.getRapidAccel()).isEqualTo(0);
        assertThat(dto.getLaneChange()).isEqualTo(0);
        assertThat(dto.getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("일부만 0값인 경우")
    void createWithPartialZeroValues() {
        // when
        TotalCountsDto dto = TotalCountsDto.of(10, 0, 5);

        // then
        assertThat(dto.getHardBrake()).isEqualTo(10);
        assertThat(dto.getRapidAccel()).isEqualTo(0);
        assertThat(dto.getLaneChange()).isEqualTo(5);
        assertThat(dto.getTotal()).isEqualTo(15);
    }

    @Test
    @DisplayName("큰 값으로 생성")
    void createWithLargeValues() {
        // when
        TotalCountsDto dto = TotalCountsDto.of(999, 888, 777);

        // then
        assertThat(dto.getHardBrake()).isEqualTo(999);
        assertThat(dto.getRapidAccel()).isEqualTo(888);
        assertThat(dto.getLaneChange()).isEqualTo(777);
        assertThat(dto.getTotal()).isEqualTo(2664);
    }

    @Test
    @DisplayName("생성자를 통한 직접 생성")
    void createWithConstructor() {
        // when
        TotalCountsDto dto = new TotalCountsDto(25, 30, 12);

        // then
        assertThat(dto.getHardBrake()).isEqualTo(25);
        assertThat(dto.getRapidAccel()).isEqualTo(30);
        assertThat(dto.getLaneChange()).isEqualTo(12);
        assertThat(dto.getTotal()).isEqualTo(67);
    }

    @Test
    @DisplayName("equals와 hashCode 테스트")
    void equalsAndHashCode() {
        // given
        TotalCountsDto dto1 = TotalCountsDto.of(10, 20, 30);
        TotalCountsDto dto2 = TotalCountsDto.of(10, 20, 30);
        TotalCountsDto dto3 = TotalCountsDto.of(15, 25, 35);

        // then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }
}