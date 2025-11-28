package com.codehows.ksisbe.user.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    private String username;
    private String password;
    private String name;
    private String dept;
    private String ranks;
    private String state;
    private LocalDateTime updateAt;
}
