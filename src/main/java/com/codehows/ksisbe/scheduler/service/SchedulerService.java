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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final SettingRepository settingRepository;
    private final UserRepository userRepository;
    private final SchedulerRepository schedulerRepository;
    private final SchedulerRepositoryCustom schedulerRepositoryCustom;
    private final SchedulerManager schedulerManager;

    //스케줄러등록
    public void createScheduler(String username, SchedulerRequestDto schedulerRequestDto) {
        User user = userRepository.findByUsernameAndIsDelete(username, "N")
                .orElseThrow(() -> new RuntimeException("유효하지 않은 유저"));

        Setting setting = settingRepository.findBySettingIdAndIsDelete(schedulerRequestDto.getSettingId(), "N")
                .orElseThrow(() -> new RuntimeException("유효하지 않은 설정"));

        String daysOfWeekStr = String.join(
                ",",
                schedulerRequestDto.getDaysOfWeek()
        );
        int executeHour = parseExecuteHour(schedulerRequestDto);
        int executeMinute = parseExecuteMinute(schedulerRequestDto);

        String displayCycle = createDisplayCycle(schedulerRequestDto);
        String displayTime = createDisplayTime(executeHour, executeMinute);

        String displayCycleCompact = displayCycle.replaceAll("\\s+", "");
        String displayTimeCompact  = displayTime.replaceAll("\\s+", "");

        String searchText = createSearchText(displayCycle, displayTime);

        Scheduler scheduler = Scheduler.builder()

                .setting(setting)
                .user(user)
                .cronExpression(schedulerRequestDto.getCronExpression())
                .daysOfWeek(daysOfWeekStr)
                .weekOfMonth(schedulerRequestDto.getWeekOfMonth())
                .executeHour(executeHour)
                .executeMinute(executeMinute)
                .displayCycle(displayCycle)
                .displayCycleCompact(displayCycleCompact)
                .displayTime(displayTime)
                .displayTimeCompact(displayTimeCompact)
                .searchText(searchText)
                .startDate(schedulerRequestDto.getStartDate())
                .endDate(schedulerRequestDto.getEndDate())
                .createAt(LocalDateTime.now())
                .isDelete("N")
                .build();
        schedulerRepository.save(scheduler);

        schedulerManager.schedule(scheduler);
    }

    //스케줄러설정조회
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

    //스케줄러 검색용 컬럼정리: 주차
    private String createDisplayCycle(SchedulerRequestDto schedulerResqestDto) {
        String weekKorean = switch(schedulerResqestDto.getWeekOfMonth()) {
            case "0" -> "매주";
            case "1" -> "첫번째 주";
            case "2" -> "두번째 주";
            case "3" -> "세번째 주";
            case "4" -> "네번째 주";
            case "5" -> "다섯번째 주";
            case "L" -> "마지막 주";
            default -> "";
        };
        String daysKorean = schedulerResqestDto.getDaysOfWeek().stream()
                .map(d -> switch (d) {
                    case "MON" -> "월요일";
                    case "TUE" -> "화요일";
                    case "WED" -> "수요일";
                    case "THU" -> "목요일";
                    case "FRI" -> "금요일";
                    case "SAT" -> "토요일";
                    case "SUN" -> "일요일";
                    default -> "";
                })
                .collect(Collectors.joining(" "));

        return weekKorean + " " + daysKorean;
    }

    //CRON 파싱
    private int parseExecuteHour(SchedulerRequestDto schedulerRequestDto) {
        return Integer.parseInt(schedulerRequestDto.getCronExpression().split(" ")[2]);
    }

    private int parseExecuteMinute(SchedulerRequestDto schedulerRequestDto) {
        return Integer.parseInt(schedulerRequestDto.getCronExpression().split(" ")[1]);
    }

    //스케줄러 검색용 컬럼정리: 시간
    private String createDisplayTime(int parseExecuteHour, int parseExecuteMinute) {
        String ampm = parseExecuteHour < 12 ? "오전" : "오후";
        int displayHour = parseExecuteHour <= 12 ? parseExecuteHour : parseExecuteHour - 12;
        if (parseExecuteMinute == 0) {
            return ampm + " " + displayHour + "시";
        }
        return ampm + " " + displayHour + "시 " + parseExecuteMinute + "분";
    }

    //검색용 문자열
    private String createSearchText(
            String displayCycle,
            String displayTime
    ) {
        return displayCycle + " " + displayTime;
    }

    //스케줄러 수정
    public void updateScheduler(String username, Long schedulerId, SchedulerRequestDto schedulerRequestDto) {
        Scheduler originScheduler = schedulerRepository.findByScheduleIdAndIsDelete(schedulerRequestDto.getSchedulerId(), "N")
                .orElseThrow(() -> new RuntimeException("유효하지 않은 스케줄러아이디"));
        User user = userRepository.findByUsernameAndIsDelete(username, "N")
                .orElseThrow(() -> new RuntimeException("유효하지 않은 유저아이디"));
        Setting setting = settingRepository.findBySettingIdAndIsDelete(schedulerRequestDto.getSettingId(), "N")
                .orElseThrow(() -> new RuntimeException("유효하지 않은 설정"));

        if (!user.getRole().equals("ROLE_ADMIN") && !originScheduler.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("수정 권한 없음");
        }

        originScheduler.setIsDelete("Y");
        originScheduler.setUpdateAt(LocalDateTime.now());
        schedulerRepository.save(originScheduler);

        //스케줄러 제거할 부분 필요

        String daysOfWeekStr = String.join(
                ",",
                schedulerRequestDto.getDaysOfWeek()
        );
        int executeHour = parseExecuteHour(schedulerRequestDto);
        int executeMinute = parseExecuteMinute(schedulerRequestDto);

        String displayCycle = createDisplayCycle(schedulerRequestDto);
        String displayTime = createDisplayTime(executeHour, executeMinute);

        String displayCycleCompact = displayCycle.replaceAll("\\s+", "");
        String displayTimeCompact  = displayTime.replaceAll("\\s+", "");

        String searchText = createSearchText(displayCycle, displayTime);

        Scheduler scheduler = Scheduler.builder()

                .setting(setting)
                .user(originScheduler.getUser())
                .cronExpression(schedulerRequestDto.getCronExpression())
                .daysOfWeek(daysOfWeekStr)
                .weekOfMonth(schedulerRequestDto.getWeekOfMonth())
                .executeHour(executeHour)
                .executeMinute(executeMinute)
                .displayCycle(displayCycle)
                .displayCycleCompact(displayCycleCompact)
                .displayTime(displayTime)
                .displayTimeCompact(displayTimeCompact)
                .searchText(searchText)
                .startDate(schedulerRequestDto.getStartDate())
                .endDate(schedulerRequestDto.getEndDate())
                .createAt(LocalDateTime.now())
                .originId(originScheduler.getScheduleId())
                .isDelete("N")
                .build();
        schedulerRepository.save(scheduler);
        schedulerManager.schedule(scheduler);
    }

    //스케줄러 삭제
    public void deleteScheduler(Long schedulerId, String username) {
        Scheduler scheduler = schedulerRepository.findByScheduleIdAndIsDelete(schedulerId, "N")
                .orElseThrow(() -> new RuntimeException("유효하지 않은 스케줄러아이디"));
        User user = userRepository.findByUsernameAndIsDelete(username, "N")
                .orElseThrow(() -> new RuntimeException("유효하지 않은 유저아이디"));

        if(!user.getRole().equals("ROLE_ADMIN") && !scheduler.getUser().getId().equals(user.getId())){
            throw new RuntimeException("삭제 권한이 없습니다.");
        }
        scheduler.setIsDelete("Y");
        schedulerRepository.save(scheduler);

        schedulerManager.schedule(scheduler);
    }
}