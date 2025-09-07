package com.smooth.driving_analysis_service.driving.controller;

import com.smooth.driving_analysis_service.driving.dto.response.DrivingCharacterResponseDto;
import com.smooth.driving_analysis_service.driving.service.CharacterService;
import com.smooth.driving_analysis_service.global.auth.AuthenticationUtils;
import com.smooth.driving_analysis_service.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/driving-analysis")
@RestController
public class CharacterController {

    private final CharacterService characterService;

    @GetMapping("/my/character")
    public ResponseEntity<ApiResponse<DrivingCharacterResponseDto>> getCurrentCharacter() {

        Long userId = AuthenticationUtils.getCurrentUserIdOrThrow();
        DrivingCharacterResponseDto responseDto = characterService.getCurrentDrivingCharacter(userId);

        return ResponseEntity.ok(ApiResponse.success("사용자의 캐릭터 성향 조회가 완료되었습니다.", responseDto));
    }
}
