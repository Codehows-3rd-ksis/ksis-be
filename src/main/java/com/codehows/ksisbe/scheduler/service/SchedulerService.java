package com.codehows.ksisbe.scheduler.service;

import com.codehows.ksisbe.scheduler.dto.SchedulerRequestDto;
import com.codehows.ksisbe.scheduler.dto.SchedulerResponseDto;
import com.codehows.ksisbe.scheduler.dto.SearchCondition;
import com.codehows.ksisbe.scheduler.entity.Scheduler;
import com.codehows.ksisbe.scheduler.repository.SchedulerRepository;
import com.codehows.ksisbe.scheduler.repository.SchedulerRepositoryCustom;
import com.codehows.ksisbe.setting.entity.Setting;
import com.codehows.ksisbe.setting.repository.SettingRepository;
import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final SettingRepository settingRepository;
    private final UserRepository userRepository;
    private final SchedulerRepository schedulerRepository;
    private final SchedulerRepositoryCustom schedulerRepositoryCustom;

    public void createScheduler(String username, SchedulerRequestDto schedulerRequestDto) {
        User user = userRepository.findByUsernameAndIsDelete(username, "N")
                .orElseThrow(() -> new RuntimeException("유효하지 않은 유저"));

        Setting setting = settingRepository.findBySettingIdAndIsDelete(schedulerRequestDto.getSettingId(), "N")
                .orElseThrow(() -> new RuntimeException("유효하지 않은 설정"));

        String daysOfWeekStr = String.join(
                ",",
                schedulerRequestDto.getDaysOfWeek()
        );

        Scheduler scheduler = Scheduler.builder()

                .setting(setting)
                .user(user)
                .cronExpression(schedulerRequestDto.getCronExpression())
                .daysOfWeek(daysOfWeekStr)
                .weekOfMonth(schedulerRequestDto.getWeekOfMonth())
                .startDate(schedulerRequestDto.getStartDate())
                .endDate(schedulerRequestDto.getEndDate())
                .createAt(LocalDateTime.now())
                .isDelete("N")
                .build();
        schedulerRepository.save(scheduler);
    }

    public Page<SchedulerResponseDto> search(
            String username,
            SearchCondition request,
            Pageable pageable
    ) {
        User user = userRepository.findByUsernameAndIsDelete(username, "N")
                .orElseThrow(() -> new RuntimeException("유효하지 않은 유저"));

        Page<Scheduler> page =
                schedulerRepositoryCustom.search(user.getId(), user.getRole(), request, pageable);

        return page.map(SchedulerResponseDto::from);
    }
}
