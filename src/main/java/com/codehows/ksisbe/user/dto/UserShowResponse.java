package com.codehows.ksisbe.user.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserShowResponse {
    private Long userId;
    private String username;
    private String name;
    private String dept;
    private String ranks;
    private LocalDateTime loginAt;
    private String state;
}
