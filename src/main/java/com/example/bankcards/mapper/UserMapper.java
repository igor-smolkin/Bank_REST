package com.example.bankcards.mapper;

import com.example.bankcards.dto.user.select.ResponseUserDto;
import com.example.bankcards.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public ResponseUserDto toDto(User user) {
        return ResponseUserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .isEnabled(user.isEnabled())
                .build();
    }
}
