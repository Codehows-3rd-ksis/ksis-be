package com.codehows.ksisbe.setting.repository;

import com.codehows.ksisbe.query.dto.SearchCondition;
import com.codehows.ksisbe.setting.entity.QSetting;
import com.codehows.ksisbe.setting.entity.Setting;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SettingRepositoryImpl implements SettingRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Setting> search(Long userId, String role, SearchCondition condition, Pageable pageable) {
        QSetting setting = QSetting.setting;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(setting.isDelete.eq("N"));

        // 관리자면 전체, 일반유저면 본인것만 조회
        if (!"ROLE_ADMIN".equals(role)) {
            builder.and(setting.user.id.eq(userId));
        }

        // 검색 조건
        if (condition != null && condition.getType() != null) {
            String keyword = condition.getKeyword();

            switch (condition.getType()) {

                case "settingName":
                    builder.and(setting.settingName.contains(keyword));
                    break;

                case "url":
                    builder.and(setting.url.contains(keyword));
                    break;

                case "userAgent":
                    builder.and(setting.userAgent.contains(keyword));
                    break;

                case "all":
                    builder.and(searchAll(keyword));
                    break;

                default:
                    break;
            }
        }

        List<Setting> content = queryFactory
                .selectFrom(setting)
                .where(builder)
                .orderBy(setting.settingId.desc()) // 최신순
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(setting.count())
                .from(setting)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression searchAll(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;

        QSetting setting = QSetting.setting;

        return setting.settingName.contains(keyword)
                .or(setting.url.contains(keyword))
                .or(setting.userAgent.contains(keyword));
    }
}
