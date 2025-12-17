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
    public Page<CrawlWork> search(SearchCondition condition, Pageable pageable) {

        QCrawlWork work = QCrawlWork.crawlWork;

        BooleanBuilder builder = new BooleanBuilder();

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
                .orderBy(work.workId.desc())
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
}