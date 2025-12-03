package com.codehows.ksisbe.setting.controller;

import com.codehows.ksisbe.ErrorResponseDto;
import com.codehows.ksisbe.setting.dto.ConditionsRequestDto;
import com.codehows.ksisbe.setting.dto.ConditionsShowDto;
import com.codehows.ksisbe.setting.dto.SettingRequestDto;
import com.codehows.ksisbe.setting.dto.SettingShowDto;
import com.codehows.ksisbe.setting.entity.Conditions;
import com.codehows.ksisbe.setting.entity.Setting;
import com.codehows.ksisbe.setting.service.SettingService;
import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;
    private final UserRepository userRepository;

    //설정등록
    @PostMapping("/setting")
    public ResponseEntity<?> createSetting(@RequestBody SettingRequestDto dto, Authentication authentication) {

        try {
            String username = authentication.getName();

            User user = userRepository.findByUsernameAndIsDelete(username, "N")
                    .orElseThrow((() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username)));
            Long userId = user.getId();

            settingService.createSetting(userId, dto);

            return ResponseEntity.ok("설정등록완료");

        }catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDto(e.getMessage()));

        }catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDto(e.getMessage()));
        }


    }

    //설정조회
    @GetMapping("/setting")
    public ResponseEntity<?> findSetting(Authentication authentication) {

        try {
            String username = authentication.getName();
            List<SettingShowDto> list = settingService.findSetting(username);
            return ResponseEntity.ok(list);

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDto(e.getMessage()));
        }
    }

    //설정수정 conditions 조회
    @GetMapping("/setting/conditions/{settingId}")
    public ResponseEntity<?> findConditions(
            @PathVariable Long settingId,
            Authentication authentication) {

        String username = authentication.getName();

        try {
            List<ConditionsShowDto> conditions = settingService.findConditions(settingId, username);
            return ResponseEntity.ok(conditions);

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDto(e.getMessage()) );

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponseDto(e.getMessage()) );

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDto(e.getMessage()) );

        }
    }
}
