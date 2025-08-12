package com.example.bankcards.controller;

import com.example.bankcards.dto.user.login.RequestLoginDto;
import com.example.bankcards.dto.user.login.ResponseLoginDto;
import com.example.bankcards.dto.user.register.RequestRegisterDto;
import com.example.bankcards.dto.user.register.ResponseRegisterDto;
import com.example.bankcards.dto.user.select.ResponseUserDto;
import com.example.bankcards.dto.user.update.RequestAdminUpdateUserDto;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class UserController {

    private final UserService userService;

    @PostMapping("/auth/register")
    public ResponseEntity<ResponseRegisterDto> register(@RequestBody RequestRegisterDto request) {
        ResponseRegisterDto response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ResponseLoginDto> login(@RequestBody RequestLoginDto request) {
        ResponseLoginDto response = userService.login(request);
        return ResponseEntity.ok().body(response);
    }

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
