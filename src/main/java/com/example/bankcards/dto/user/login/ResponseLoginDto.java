package com.example.bankcards.dto.user.login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ResponseLoginDto {
    private String accessToken;
    private String refreshToken;
}
