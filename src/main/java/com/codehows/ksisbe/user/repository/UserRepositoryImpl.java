package com.codehows.ksisbe.user.repository;

import com.codehows.ksisbe.user.QUser;
import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.query.dto.SearchCondition;
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
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<User> search(SearchCondition condition, Pageable pageable) {
        QUser user = QUser.user;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(user.isDelete.eq("N"));
        builder.and(user.role.ne("ROLE_ADMIN"));

        // 조건 분기
        switch (condition.getType()) {
            case "userId":
                builder.and(user.username.contains(condition.getKeyword()));
                break;

            case "name":
                builder.and(user.name.contains(condition.getKeyword()));
                break;

            case "dept":
                builder.and(user.dept.contains(condition.getKeyword()));
                break;

            case "ranks":
                builder.and(user.ranks.contains(condition.getKeyword()));
                break;

            case "loginAt":
                builder.and(user.loginAt.stringValue().contains(condition.getKeyword()));
                break;

            case "all":
                builder.and(searchAll(condition.getKeyword()));
            default:
                // 아무 조건 적용 안함
                break;
        }
        List<User> content = queryFactory
                .selectFrom(user)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(user.createAt.desc()) // 최신 생성 내림차순
                .fetch();

        long total = queryFactory
                .selectFrom(user)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression searchAll(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;

        QUser user = QUser.user;

        return
                user.username.contains(keyword)
                        .or(user.id.stringValue().contains(keyword))  // PK
                        .or(user.name.contains(keyword))
                        .or(user.dept.contains(keyword))
                        .or(user.ranks.contains(keyword))
                        .or(user.loginAt.stringValue().contains(keyword));
    }
}
