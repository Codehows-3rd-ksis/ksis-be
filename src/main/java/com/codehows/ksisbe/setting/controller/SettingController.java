package com.codehows.ksisbe.setting.controller;

import com.codehows.ksisbe.query.dto.SearchCondition;
import com.codehows.ksisbe.setting.dto.ConditionsShowDto;
import com.codehows.ksisbe.setting.dto.SettingRequestDto;
import com.codehows.ksisbe.setting.dto.SettingShowDto;
import com.codehows.ksisbe.setting.dto.SettingUpdateDto;
import com.codehows.ksisbe.setting.entity.Setting;
import com.codehows.ksisbe.setting.service.SettingService;
import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    //설정조회
    @GetMapping("/setting")
    public ResponseEntity<?> findSetting(Authentication authentication,
                                         @ModelAttribute SearchCondition condition, Pageable pageable) {

        try {
            String username = authentication.getName();
            Page<SettingShowDto> result = settingService.findSetting(username, condition, pageable);
            return ResponseEntity.ok(result);

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    //설정수정 conditions 조회
    @GetMapping("/setting/conditions/{settingId}")
    public ResponseEntity<?> findConditions(@PathVariable Long settingId, Authentication authentication) {

        String username = authentication.getName();

        try {
            List<ConditionsShowDto> conditions = settingService.findConditions(settingId, username);
            return ResponseEntity.ok(conditions);

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));

        }
    }

    //설정수정
    @PutMapping("/setting/{settingId}")
    public ResponseEntity<?> updateSetting(@PathVariable Long settingId,
                                           @RequestBody SettingUpdateDto settingUpdateDto, Authentication authentication) {

        String username = authentication.getName();

        try {
            User user = userRepository.findByUsernameAndIsDelete(username, "N")
                    .orElseThrow((() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username)));
            Long userId = user.getId();
            settingService.updateSetting(userId, settingId, settingUpdateDto);

            return ResponseEntity.ok("수정완료");

        }catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    //설정삭제
    @DeleteMapping("/setting/{settingId}")
    public ResponseEntity<?> deleteSetting(@PathVariable Long settingId, Authentication authentication) {
        String username = authentication.getName();
        settingService.deleteSetting(settingId, username);
        return ResponseEntity.ok("삭제완료");
    }

}
