package com.codehows.ksisbe.history.repository;

import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.crawling.entity.QCrawlWork;
import com.codehows.ksisbe.query.dto.SearchCondition;
import com.codehows.ksisbe.setting.entity.QSetting;
import com.codehows.ksisbe.setting.entity.Setting;
import com.codehows.ksisbe.setting.repository.SettingRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CrawlWorkHistoryRepositoryImpl implements CrawlWorkHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CrawlWork> search(Long userId, String role, SearchCondition condition, Pageable pageable) {
        QCrawlWork work = QCrawlWork.crawlWork;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(work.isDelete.eq("N"));

        // 관리자면 전체, 일반유저면 본인것만 조회
        if (!"ROLE_ADMIN".equals(role)) {

            BooleanExpression manualCondition =
                    work.type.eq("수동실행")
                            .and(work.startedBy.id.eq(userId));

            BooleanExpression scheduleCondition =
                    work.type.eq("스케줄링")
                            .and(work.setting.user.id.eq(userId));

            builder.and(manualCondition.or(scheduleCondition));
        }

        // 🔹 실행 타입
        builder.and(eqType(work, condition.getType()));

        // 🔹 키워드 (필요 시)
        builder.and(containsKeyword(work, condition.getKeyword()));

        // 🔹 기간 조건 (AND)
        builder.and(betweenDate(
                work.startAt,
                work.endAt,
                condition.getStartDate(),
                condition.getEndDate()
        ));

        List<CrawlWork> content = queryFactory
                .selectFrom(work)
                .where(builder)
                .orderBy(work.createAt.desc()) // 최신순 내림차순
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectFrom(work)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression eqType(QCrawlWork work, String type) {
        if (type == null || type.equals("all")) return null;
        return work.type.eq(type); // 수동실행 / 스케줄링
    }

    private BooleanExpression containsKeyword(QCrawlWork work, String keyword) {
        if (keyword == null || keyword.isBlank()) return null;

        return work.setting.settingName.contains(keyword);
    }

    private BooleanBuilder  betweenDate(
            DateTimePath<LocalDateTime> startAt,
            DateTimePath<LocalDateTime> endAt,
            String startDate,
            String endDate
    ) {
        if ((startDate == null || startDate.isEmpty()) && (endDate == null || endDate.isEmpty())) return null;

        LocalDateTime start = startDate != null
                ? LocalDate.parse(startDate).atStartOfDay()
                : null;

        LocalDateTime end = endDate != null
                ? LocalDate.parse(endDate).atTime(LocalTime.MAX)
                : null;

        BooleanBuilder builder = new BooleanBuilder();

        if (start != null) {
            builder.and(endAt.goe(start));
        }
        if (end != null) {
            builder.and(startAt.loe(end));
        }

        return builder;
    }

    public Page<CrawlWork> searchByUserLog(
            Long userId,
            SearchCondition condition,
            Pageable pageable
    ) {
        QCrawlWork work = QCrawlWork.crawlWork;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(work.isDelete.eq("N"));

        // 🔹 userId 기준 조회 (항상 적용)
        BooleanExpression manualCondition =
                work.type.eq("수동실행")
                        .and(work.startedBy.id.eq(userId));

        BooleanExpression scheduleCondition =
                work.type.eq("스케줄링")
                        .and(work.setting.user.id.eq(userId));

        builder.and(manualCondition.or(scheduleCondition));

        // 🔹 실행 타입 필터 (수동실행 / 스케줄링 / all)
        builder.and(eqType(work, condition.getType()));

        // 🔹 키워드
        builder.and(containsKeyword(work, condition.getKeyword()));

        // 🔹 기간
        builder.and(betweenDate(
                work.startAt,
                work.endAt,
                condition.getStartDate(),
                condition.getEndDate()
        ));

        List<CrawlWork> content = queryFactory
                .selectFrom(work)
                .where(builder)
                .orderBy(work.createAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectFrom(work)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }
}