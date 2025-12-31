package com.codehows.ksisbe.scheduler.service;

import com.codehows.ksisbe.scheduler.entity.Scheduler;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ScheduleValidator {

    public static boolean isExecutable(Scheduler scheduler) {

        //오늘 날짜
        LocalDate today = LocalDate.now();


        //시작일 / 종료일 체크
        if (scheduler.getStartDate() != null) {
            if (today.isBefore(scheduler.getStartDate())) {
                return false;
            }
        }

        if (scheduler.getEndDate() != null) {
            if (today.isAfter(scheduler.getEndDate())) {
                return false;
            }
        }


        //요일 체크
        if (scheduler.getDaysOfWeek() != null) {

            List<String> allowedDays =
                    Arrays.asList(scheduler.getDaysOfWeek().split(","));

            // 오늘 요일  MON, TUE 형태로 변환
            String todayDay =
                    today.getDayOfWeek().name().substring(0, 3);

            if (!allowedDays.contains(todayDay)) {
                return false;
            }
        }


        //주차 체크
        String weekOfMonth = scheduler.getWeekOfMonth();

        // 0이면 매주 실행
        if ("0".equals(weekOfMonth)) {
            return true;
        }

        // 오늘이 몇 번째 주인지 계산
        int todayWeek =
                today.get(WeekFields.of(Locale.KOREA).weekOfMonth());

        // 이번 달의 마지막 주차 계산
        int lastWeekOfMonth =
                today.withDayOfMonth(today.lengthOfMonth())
                        .get(WeekFields.of(Locale.KOREA).weekOfMonth());

        // 마지막 주(L)
        if ("L".equals(weekOfMonth)) {
            return todayWeek == lastWeekOfMonth;
        }

        // 숫자 주차 (1~5)
        int targetWeek = Integer.parseInt(weekOfMonth);

        return todayWeek == targetWeek;
    }
}
