package com.example.bankcards.controller;

import com.example.bankcards.dto.user.select.ResponseUserDto;
import com.example.bankcards.dto.user.update.RequestAdminUpdateUserDto;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class UserController {

    private final UserService userService;

    @PatchMapping("/api/users/{userId}/disable")
    public ResponseEntity<ResponseUserDto> disableUser(@PathVariable UUID userId) {
        ResponseUserDto response = userService.disableUser(userId);
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping("/api/users/{userId}/enable")
    public ResponseEntity<ResponseUserDto> enableUser(@PathVariable UUID userId) {
        ResponseUserDto response = userService.enableUser(userId);
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping("/api/users/{usersId}/update")
    public ResponseEntity<ResponseUserDto> updateUser(@PathVariable UUID usersId,
                                                      @RequestBody RequestAdminUpdateUserDto request) {
        ResponseUserDto response = userService.updateUserByAdmin(usersId, request);
        return ResponseEntity.ok().body(response);
    }
}
