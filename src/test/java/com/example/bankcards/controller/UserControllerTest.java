package com.example.bankcards.controller;

import com.example.bankcards.dto.user.select.ResponseUserDto;
import com.example.bankcards.dto.user.update.RequestAdminUpdateUserDto;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.security.config.JwtAuthenticationEntryPoint;
import com.example.bankcards.security.filter.JwtAuthenticationFilter;
import com.example.bankcards.security.service.CustomUserDetailsService;
import com.example.bankcards.security.service.JwtService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private UserService userService;

    @Test
    void disableUser_success() throws Exception {
        UUID userId = UUID.randomUUID();

        ResponseUserDto responseDto = ResponseUserDto.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .role(UserRole.USER)
                .isEnabled(true)
                .build();

        when(userService.disableUser(userId)).thenReturn(responseDto);

        mockMvc.perform(patch("/api/users/{userId}/disable", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.enabled").value(true));

        verify(userService).disableUser(userId);
    }

    @Test
    void disableUser_notFound_throwsNotFoundException() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.disableUser(userId)).thenThrow(new NotFoundException("user not found"));

        mockMvc.perform(patch("/api/users/{userId}/disable", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("user not found"));
    }

    @Test
    void disableUser_alreadyDisabled_throwsConflictException() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.disableUser(userId)).thenThrow(new ConflictException("user already disabled"));

        mockMvc.perform(patch("/api/users/{userId}/disable", userId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("user already disabled"));
    }

    @Test
    void enableUser_success() throws Exception {
        UUID userId = UUID.randomUUID();

        ResponseUserDto responseDto = ResponseUserDto.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .role(UserRole.USER)
                .isEnabled(true)
                .build();

        when(userService.enableUser(userId)).thenReturn(responseDto);

        mockMvc.perform(patch("/api/users/{userId}/enable", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.enabled").value(true));

        verify(userService).enableUser(userId);
    }

    @Test
    void enableUser_notFound_throwsNotFoundException() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.enableUser(userId)).thenThrow(new NotFoundException("user not found"));

        mockMvc.perform(patch("/api/users/{userId}/enable", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("user not found"));
    }

    @Test
    void enableUser_alreadyEnabled_throwsConflictException() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.enableUser(userId)).thenThrow(new ConflictException("user already enabled"));

        mockMvc.perform(patch("/api/users/{userId}/enable", userId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("user already enabled"));
    }

    @Test
    void updateUserByAdmin_success() throws Exception {
        UUID userId = UUID.randomUUID();

        RequestAdminUpdateUserDto requestDto = RequestAdminUpdateUserDto.builder()
                .email("new@example.com")
                .role(UserRole.ADMIN)
                .build();

        ResponseUserDto responseDto = ResponseUserDto.builder()
                .id(userId)
                .email("new@example.com")
                .role(UserRole.ADMIN)
                .isEnabled(true)
                .build();

        when(userService.updateUserByAdmin(eq(userId), any(RequestAdminUpdateUserDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/api/users/{usersId}/update", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.enabled").value(true));

        verify(userService).updateUserByAdmin(eq(userId), any(RequestAdminUpdateUserDto.class));
    }

    @Test
    void updateUserByAdmin_notFound_throwsNotFoundException() throws Exception {
        UUID userId = UUID.randomUUID();

        RequestAdminUpdateUserDto requestDto = RequestAdminUpdateUserDto.builder()
                .email("new@example.com")
                .role(UserRole.ADMIN)
                .build();

        when(userService.updateUserByAdmin(eq(userId), any(RequestAdminUpdateUserDto.class)))
                .thenThrow(new NotFoundException("user not found"));

        mockMvc.perform(patch("/api/users/{usersId}/update", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("user not found"));

        verify(userService).updateUserByAdmin(eq(userId), any(RequestAdminUpdateUserDto.class));
    }
}
