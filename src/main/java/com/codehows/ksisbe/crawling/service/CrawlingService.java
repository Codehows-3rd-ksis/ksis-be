package com.codehows.ksisbe.crawling.service;

import com.codehows.ksisbe.scheduler.entity.Scheduler;
import com.codehows.ksisbe.scheduler.repository.SchedulerRepository;
import com.codehows.ksisbe.scheduler.service.SchedulerService;
import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.crawling.repository.CrawlWorkRepository;
import com.codehows.ksisbe.setting.entity.Setting;
import com.codehows.ksisbe.setting.repository.SettingRepository;
import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.user.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class CrawlingService {

    private final UserRepository userRepository;
    private final SettingRepository settingRepository;
    private final StartSingleCrawlingService startSingleCrawlingService;
    private final StartMultipleCrawlingService startMultipleCrawlingService;
    private final SchedulerRepository schedulerRepository;

    //수동실행
    private final CrawlWorkRepository crawlWorkRepository;

    public void startCrawling(Long settingId, String username) {

        startCrawlingInternal(settingId, username, null);
    }

    //스케줄러(배치전용)
    public void startCrawlingBySchedule(Long schedulerId, Long settingId) {
        Scheduler scheduler = schedulerRepository.findById(schedulerId)
                .orElseThrow(() -> new RuntimeException("스케줄 없음"));

        startCrawlingInternal(settingId, null, scheduler);
    }

    public void startCrawlingInternal(Long settingId, String username, Scheduler scheduler) {
        Setting setting = settingRepository.findBySettingIdAndIsDelete(settingId, "N")
                .orElseThrow(() -> new RuntimeException("유효하지 않은 설정"));

        User user = null;

        if (scheduler == null) {
            //수동 실행
            user = userRepository.findByUsernameAndIsDelete(username, "N")
                    .orElseThrow(() -> new RuntimeException("유효하지 않은 유저"));

            if (!user.getRole().equals("ROLE_ADMIN")
                    && !setting.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("수집 권한 없음");
            }
        }

        // scheduler != null 이면 스케줄 실행 (권한 검사 X)
        String type = setting.getType();
        if ("단일".equalsIgnoreCase(type)) {
            startSingleCrawlingService.startSingleCrawling(settingId, user, scheduler);
        } else if ("다중".equalsIgnoreCase(type)) {
            startMultipleCrawlingService.startMultipleCrawling(settingId, user, scheduler);
        } else {
            throw new RuntimeException("알 수 없는 설정 타입: " + type);
        }
    }

    @Transactional
    public void requestStop(Long workId) {
        CrawlWork work = crawlWorkRepository.findById(workId)
                .orElseThrow();

        if (!"RUNNING".equals(work.getState())) {
            return; // 이미 종료됨
        }

        work.setState("STOP_REQUEST");
        work.setUpdateAt(LocalDateTime.now());
    }
}