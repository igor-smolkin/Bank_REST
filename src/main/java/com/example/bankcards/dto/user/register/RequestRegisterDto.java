package com.example.bankcards.dto.user.register;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestRegisterDto {
    private String email;
    private String password;
}
