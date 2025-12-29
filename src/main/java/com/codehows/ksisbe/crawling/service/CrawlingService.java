package com.codehows.ksisbe.crawling.service;

import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.crawling.repository.CrawlWorkRepository;
import com.codehows.ksisbe.setting.entity.Setting;
import com.codehows.ksisbe.setting.repository.SettingRepository;
import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.user.repository.UserRepository;
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
    private final CrawlWorkRepository crawlWorkRepository;

//    @Async  /* Controller에서 taskExecutor.execute 사용중이므로 @Async를 또 쓰면 이중 비동기 */
    public void startCrawling(Long settingId, String username) {
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
            startMultipleCrawlingService.startMultipleCrawling(settingId, user);
        }
        else {
            throw new RuntimeException("알 수 없는 설정 타입니다: " + type);
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