package com.example.bankcards.dto.user.select;

import com.example.bankcards.entity.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ResponseUserDto {
    private UUID id;
    private String email;
    private UserRole role;
    private boolean isEnabled;
}
