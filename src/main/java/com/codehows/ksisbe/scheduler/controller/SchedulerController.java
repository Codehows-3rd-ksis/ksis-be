package com.codehows.ksisbe.scheduler.controller;

import com.codehows.ksisbe.scheduler.dto.SchedulerRequestDto;
import com.codehows.ksisbe.scheduler.service.SchedulerService;
import com.codehows.ksisbe.setting.repository.SettingRepository;
import com.codehows.ksisbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SchedulerController {

    private final UserRepository userRepository;
    private final SettingRepository settingRepository;
    private final SchedulerService schedulerService;

    @PostMapping("/scheduler")
    public ResponseEntity<?> createScheduler(@RequestBody SchedulerRequestDto schedulerRequestDto, Authentication authentication) {
        String username = authentication.getName();
        schedulerService.createScheduler(username, schedulerRequestDto);
        return ResponseEntity.ok("스케줄러등록완료");
    }
}
