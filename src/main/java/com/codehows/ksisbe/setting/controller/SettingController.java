package com.codehows.ksisbe.setting.controller;

import com.codehows.ksisbe.setting.entity.Setting;
import com.codehows.ksisbe.setting.service.SettingService;
import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;
    private final UserRepository userRepository;

    @PostMapping("/setting")
    public ResponseEntity<Setting> createSetting(@RequestBody Setting setting, Authentication authentication) {

        String username = authentication.getName();

        User user = userRepository.findByUsernameAndIsDelete(username, "N")
                .orElseThrow((() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username)));
        Long userId = user.getId();

        Setting save = settingService.createSetting(userId, setting);
        return ResponseEntity.ok().body(save);
    }
}
