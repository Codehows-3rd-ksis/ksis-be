package com.codehows.ksisbe.user.service;

import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.user.dto.UserRegisterRequest;
import com.codehows.ksisbe.user.dto.UserUpdateRequest;
import com.codehows.ksisbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(UserRegisterRequest dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role("ROLE_USER") // 기본 ROLE_USER
                .name(dto.getName())
                .dept(dto.getDept())
                .ranks(dto.getRanks())
                .state(dto.getState())
                .isDelete("N")
                .createAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

    public void updateUser(Long id, UserUpdateRequest dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + id));

        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setName(dto.getName());
        user.setDept(dto.getDept());
        user.setRanks(dto.getRanks());
        user.setState(dto.getState());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        userRepository.save(user);
    }
}
