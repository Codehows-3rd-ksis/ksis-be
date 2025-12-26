package com.codehows.ksisbe.scheduler.controller;

import com.codehows.ksisbe.scheduler.dto.SchedulerRequestDto;
import com.codehows.ksisbe.scheduler.dto.SchedulerResponseDto;
import com.codehows.ksisbe.scheduler.dto.SearchCondition;
import com.codehows.ksisbe.scheduler.service.SchedulerService;
import com.codehows.ksisbe.setting.repository.SettingRepository;
import com.codehows.ksisbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SchedulerController {

    private final UserRepository userRepository;
    private final SettingRepository settingRepository;
    private final SchedulerService schedulerService;

    //스케줄러등록
    @PostMapping("/scheduler")
    public ResponseEntity<?> createScheduler(@RequestBody SchedulerRequestDto schedulerRequestDto, Authentication authentication) {
        String username = authentication.getName();
        schedulerService.createScheduler(username, schedulerRequestDto);
        return ResponseEntity.ok("스케줄러등록완료");
    }

    //스케줄러조회
    @GetMapping("/scheduler")
    public ResponseEntity<?> findSchedulers(Authentication authentication,
                                            @ModelAttribute SearchCondition searchCondition, Pageable pageable) {

        String username = authentication.getName();
        Page<SchedulerResponseDto> result =
                schedulerService.search(username, searchCondition, pageable);

        return ResponseEntity.ok(result);
    }

    //스케줄러수정


}
