package com.codehows.ksisbe.user.controller;

import com.codehows.ksisbe.user.dto.UserRegisterRequest;
import com.codehows.ksisbe.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/user")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterRequest dto) {
        userService.registerUser(dto);
        return ResponseEntity.ok("등록완료");
    }
}
