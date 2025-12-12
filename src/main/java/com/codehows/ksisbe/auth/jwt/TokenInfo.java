package com.codehows.ksisbe.auth.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class TokenInfo {
    private String GrantType;
    private String accessToken;
}

