package com.example.bankcards.dto.user.update;

import com.example.bankcards.entity.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestAdminUpdateUserDto {
    private String email;
    private UserRole role;
}
