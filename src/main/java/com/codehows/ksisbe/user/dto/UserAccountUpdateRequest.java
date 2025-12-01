package com.codehows.ksisbe.user.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountUpdateRequest {
    private String username;
    private String password;
    private LocalDateTime updateAt;
}
