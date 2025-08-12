package com.example.bankcards.dto.token.refresh;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseRefreshDto {
    private String accessToken;
    private String refreshToken;
}
