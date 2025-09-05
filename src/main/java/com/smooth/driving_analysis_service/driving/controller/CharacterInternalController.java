package com.smooth.driving_analysis_service.driving.controller;


import com.smooth.driving_analysis_service.driving.dto.response.UserCharacterBulkResponseDto;
import com.smooth.driving_analysis_service.driving.dto.response.UserCharacterResponseDto;
import com.smooth.driving_analysis_service.driving.service.CharacterService;
import com.smooth.driving_analysis_service.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/internal/v1/characters")
@RestController
public class CharacterInternalController {

    private final CharacterService characterService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserCharacterBulkResponseDto>> getAllUsersCharacter(
            @RequestParam(required = false) boolean hasCharacter){

        UserCharacterBulkResponseDto responseDto = characterService.getAllUsersCharacter(hasCharacter);

        return ResponseEntity.ok(ApiResponse.success("모든 사용자의 캐릭터 조회가 완료되었습니다.", responseDto));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserCharacterResponseDto>> getUserCharacter(
            @PathVariable Long userId
    ){
        UserCharacterResponseDto responseDto = characterService.getUserCharacter(userId);
        return ResponseEntity.ok(ApiResponse.success("사용자 캐릭터 조회가 완료되었습니다.", responseDto));
    }
}
