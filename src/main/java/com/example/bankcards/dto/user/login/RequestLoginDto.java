package com.example.bankcards.dto.user.login;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestLoginDto {
    private String email;
    private String password;
}
