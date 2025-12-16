package com.codehows.ksisbe.status.controller;

import com.codehows.ksisbe.setting.dto.SettingShowDto;
import com.codehows.ksisbe.status.dto.StatusShowDto;
import com.codehows.ksisbe.status.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class StatusController {

    private final StatusService statusService;

    @GetMapping("/status")
    public ResponseEntity<?> findAll(Authentication authentication) {

        try {
            String username = authentication.getName();
            List<StatusShowDto> list = statusService.findStatus(username);
            return ResponseEntity.ok(list);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
