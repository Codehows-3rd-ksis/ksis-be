package com.codehows.ksisbe.user.repository;

import com.codehows.ksisbe.setting.repository.SettingRepository;
import com.codehows.ksisbe.setting.repository.SettingRepositoryCustom;
import com.codehows.ksisbe.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long>  {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndIsDelete(String username, String isDelete);
    List<User> findAllByIsDelete(String isDelete);
    List<User> findAllByIsDeleteAndRoleNot(String isDelete, String role);


}
