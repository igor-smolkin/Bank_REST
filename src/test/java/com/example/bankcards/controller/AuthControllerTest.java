package com.example.bankcards.controller;

import com.example.bankcards.dto.user.login.RequestLoginDto;
import com.example.bankcards.dto.user.login.ResponseLoginDto;
import com.example.bankcards.dto.user.register.RequestRegisterDto;
import com.example.bankcards.dto.user.register.ResponseRegisterDto;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.LoginFailedException;
import com.example.bankcards.security.config.JwtAuthenticationEntryPoint;
import com.example.bankcards.security.filter.JwtAuthenticationFilter;
import com.example.bankcards.security.service.CustomUserDetailsService;
import com.example.bankcards.security.service.JwtService;
import com.example.bankcards.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private AuthService authService;

    @Test
    void register_success_returnCreated() throws Exception {
        RequestRegisterDto request = RequestRegisterDto.builder()
                .email("user@example.com")
                .password("password")
                .build();

        ResponseRegisterDto response = ResponseRegisterDto.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .build();

        when(authService.register(Mockito.any(RequestRegisterDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.getId().toString()))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void register_userAlreadyExists_throwsConflictException() throws Exception {
        RequestRegisterDto request = RequestRegisterDto.builder()
                .email("user@example.com")
                .password("password")
                .build();

        when(authService.register(Mockito.any(RequestRegisterDto.class)))
                .thenThrow(new ConflictException("already exists"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("already exists"));
    }

    @Test
    void login_success_returnOk() throws Exception {
        RequestLoginDto request = RequestLoginDto.builder()
                .email("user@example.com")
                .password("password")
                .build();

        ResponseLoginDto response = ResponseLoginDto.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        when(authService.login(Mockito.any(RequestLoginDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void login_badCredentials_returnUnauthorized() throws Exception {
        RequestLoginDto request = RequestLoginDto.builder()
                .email("user@example.com")
                .password("wrong-password")
                .build();

        when(authService.login(Mockito.any(RequestLoginDto.class)))
                .thenThrow(new LoginFailedException("bad credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
