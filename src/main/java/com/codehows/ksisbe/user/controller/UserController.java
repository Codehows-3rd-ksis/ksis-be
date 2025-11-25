package com.codehows.ksisbe.user.controller;

import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.user.dto.UserProfileResponse;
import com.codehows.ksisbe.user.dto.UserRegisterRequest;
import com.codehows.ksisbe.user.dto.UserShowResponse;
import com.codehows.ksisbe.user.dto.UserUpdateRequest;
import com.codehows.ksisbe.user.repository.UserRepository;
import com.codehows.ksisbe.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    //관리자 유저등록
    @PostMapping("/user")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterRequest dto) {
        userService.registerUser(dto);
        return ResponseEntity.ok("등록완료");
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

    //관리자 유저조회
    @GetMapping("/user")
    public ResponseEntity<List<UserShowResponse>> findAllUsers() {
        List<User> users = userRepository.findAllByIsDelete("N");

        List<UserShowResponse> findList = users.stream()
                .map(user -> UserShowResponse.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .name(user.getName())
                        .dept(user.getDept())
                        .ranks(user.getRanks())
                        .loginAt(user.getLoginAt())
                        .state(user.getState())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(findList);
    }
    
    //관리자 유저수정
    @PutMapping("/user/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest dto) {
        userService.updateUser(id, dto);
        return ResponseEntity.ok("수정완료");
    }

    //관리자 유저삭제
    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("삭제완료");
    }
}
