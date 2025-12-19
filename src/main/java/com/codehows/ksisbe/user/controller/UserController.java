package com.codehows.ksisbe.user.controller;

import com.codehows.ksisbe.query.dto.CustomPageResponse;
import com.codehows.ksisbe.query.dto.SearchCondition;
import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.user.dto.*;
import com.codehows.ksisbe.user.repository.UserRepository;
import com.codehows.ksisbe.user.repository.UserRepositoryImpl;
import com.codehows.ksisbe.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserRepositoryImpl userRepositoryImpl;

    //관리자 유저등록
    @PostMapping("/user")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterRequest dto) {
        userService.registerUser(dto);
        return ResponseEntity.ok("등록완료");
    }
    @GetMapping("/user/checkUsername")
    public ResponseEntity<?> checkUsernameDuplicate(
            @RequestParam String username) {

        boolean isDuplicate = userService.isUsernameDuplicate(username);

        return ResponseEntity.ok(
                Map.of(
                        "duplicate", isDuplicate,
                        "message", isDuplicate ? "입력한 ID는 이미 사용 중인 ID입니다." : "사용 가능한 ID입니다."
                )
        );
    }

    //로그인 유저정보 가져오기
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> findUserInfo(Authentication authentication) {
        // authentication.getName()은 JWT sub (username)를 반환
        String username = authentication.getName();

        User user = userRepository.findByUsernameAndIsDelete(username, "N")
                .orElseThrow((() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username)));

        UserProfileResponse response = UserProfileResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .build();

        return ResponseEntity.ok(response);
    }

    //관리자 유저조회 테이블조회용
    @GetMapping("/user")
    public ResponseEntity<?> findAllUsers(
            @ModelAttribute SearchCondition condition, Pageable pageable){

        Page<User> users = userRepositoryImpl.search(condition, pageable);


        Page<UserShowResponse> result = users.map(user -> UserShowResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .name(user.getName())
                .dept(user.getDept())
                .ranks(user.getRanks())
                .loginAt(user.getLoginAt())
                .state(user.getState())
                .build()
        );

        CustomPageResponse<UserShowResponse> response =
                new CustomPageResponse<>(
                        result.getContent(),
                        result.getNumber(),
                        result.getSize(),
                        result.getTotalElements(),
                        result.getTotalPages()
                );

        return ResponseEntity.ok(response);
    }
    
    //관리자 유저 정보 수정
    @PutMapping("/userInfo/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest dto) {
        userService.updateUser(id, dto);
        return ResponseEntity.ok("수정완료");
    }
    //관리자 유저 계정 수정
    @PutMapping("/userAccount/{id}")
    public ResponseEntity<?> updateUserAccount(@PathVariable Long id, @RequestBody UserAccountUpdateRequest dto) {
        userService.updateUserAccount(id, dto);
        return ResponseEntity.ok("수정완료");
    }
    @GetMapping("/user/checkUsername/{id}")
    public ResponseEntity<?> checkUsernameDuplicateForUpdate(
            @PathVariable Long id,
            @RequestParam String username) {

        boolean isDuplicate = userService.isUsernameDuplicate(username, id);

        return ResponseEntity.ok(
                Map.of(
                        "duplicate", isDuplicate,
                        "message", isDuplicate ? "입력한 ID는 이미 사용 중인 ID입니다." : "사용 가능한 ID입니다."
                )
        );
    }


    //관리자 유저삭제
    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("삭제완료");
    }
}
