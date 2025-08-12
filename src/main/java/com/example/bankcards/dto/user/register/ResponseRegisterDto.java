package com.example.bankcards.dto.user.register;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ResponseRegisterDto {

    private UUID id;
    private String email;
}
