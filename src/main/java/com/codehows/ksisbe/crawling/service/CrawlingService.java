package com.codehows.ksisbe.crawling.service;

import com.codehows.ksisbe.scheduler.entity.Scheduler;
import com.codehows.ksisbe.scheduler.repository.SchedulerRepository;
import com.codehows.ksisbe.scheduler.service.SchedulerService;
import com.codehows.ksisbe.setting.entity.Setting;
import com.codehows.ksisbe.setting.repository.SettingRepository;
import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CrawlingService {

    private final UserRepository userRepository;
    private final SettingRepository settingRepository;
    private final StartSingleCrawlingService startSingleCrawlingService;
    private final StartMultipleCrawlingService startMultipleCrawlingService;
    private final SchedulerRepository schedulerRepository;

    //수동실행
    public void startCrawling(Long settingId, String username) {
        startCrawlingInternal(settingId, username, null);
    }

    //스케줄러(배치전용)
    public void startCrawlingBySchedule(
            Long schedulerId,
            Long settingId,
            String username
    ) {
        Scheduler scheduler = schedulerRepository.findById(schedulerId)
                .orElseThrow(() -> new RuntimeException("스케줄 없음"));

        startCrawlingInternal(settingId, username, scheduler);
    }


    public void startCrawlingInternal(Long settingId, String username, Scheduler scheduler) {
        User user = userRepository.findByUsernameAndIsDelete(username, "N")
                .orElseThrow(() -> new RuntimeException("유효한 유저입니다."));

        Setting setting = settingRepository.findBySettingIdAndIsDelete(settingId, "N")
                .orElseThrow(() -> new RuntimeException("유효한 설정아이디 입니다."));

        if (!user.getRole().equals("ROLE_ADMIN") && !setting.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("수집권한이 없습니다.");
        }
        String type = setting.getType();
        if ("단일".equalsIgnoreCase(type)) {
            startSingleCrawlingService.startSingleCrawling(settingId, user);
        }
        else if ("다중".equalsIgnoreCase(type)) {
            startMultipleCrawlingService.startMultipleCrawling(settingId, user, scheduler);
        }
        else {
            throw new RuntimeException("알 수 없는 설정 타입니다: " + type);
        }
    }
}