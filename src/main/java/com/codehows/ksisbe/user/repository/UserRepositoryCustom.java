package com.codehows.ksisbe.user.repository;

import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.query.dto.SearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    Page<User> search(SearchCondition condition, Pageable pageable);
}
