package com.codehows.ksisbe.scheduler.repository;

import com.codehows.ksisbe.scheduler.dto.SearchCondition;
import com.codehows.ksisbe.scheduler.entity.QScheduler;
import com.codehows.ksisbe.scheduler.entity.Scheduler;
import com.codehows.ksisbe.setting.entity.QSetting;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SchedulerRepositoryImpl implements SchedulerRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Scheduler> search(
            Long userId,
            String role,
            SearchCondition request,
            Pageable pageable
    ) {
        QScheduler scheduler = QScheduler.scheduler;
        QSetting setting = QSetting.setting;

        BooleanBuilder builder = new BooleanBuilder();

        // 기본 조건
        builder.and(scheduler.isDelete.eq("N"));

        // 사용자 권한
        if (!"ROLE_ADMIN".equals(role)) {
            builder.and(scheduler.user.id.eq(userId));
        }

        // 기간 검색
        if (request.getStartDate() != null) {
            builder.and(scheduler.startDate.goe(request.getStartDate()));
        }
        if (request.getEndDate() != null) {
            builder.and(scheduler.endDate.loe(request.getEndDate()));
        }

        // 검색 타입별 분기
        if (request.getType() != null && request.getKeyword() != null) {
            switch (request.getType()) {
                case "settingName":
                    builder.and(setting.settingName.contains(request.getKeyword()));
                    break;

                case "cycle":
                    builder.and(
                            scheduler.displayCycleCompact.contains(
                                    request.getKeyword().replaceAll("\\s+", "")
                            )
                    );
                    break;

                case "collectAt":
                    builder.and(
                            scheduler.displayTimeCompact.contains(
                                    request.getKeyword().replaceAll("\\s+", "")
                            )
                    );
                    break;

                case "all":
                default:
                    String keywordCompact = request.getKeyword().replaceAll("\\s+", "");

                    builder.andAnyOf(
                            setting.settingName.contains(request.getKeyword()),
                            scheduler.displayCycleCompact.contains(keywordCompact),
                            scheduler.displayTimeCompact.contains(keywordCompact)
                    );
            }
        }

        List<Scheduler> content = queryFactory
                .selectFrom(scheduler)
                .leftJoin(scheduler.setting, setting).fetchJoin()
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(scheduler.createAt.desc())
                .fetch();

        Long total = queryFactory
                .select(scheduler.count())
                .from(scheduler)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
