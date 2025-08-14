package com.example.bankcards.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.example.bankcards.dto.user.login.RequestLoginDto;
import com.example.bankcards.dto.user.login.ResponseLoginDto;
import com.example.bankcards.dto.user.register.RequestRegisterDto;
import com.example.bankcards.dto.user.register.ResponseRegisterDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.LoginFailedException;
import com.example.bankcards.repository.RefreshTokenRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_success() {
        RequestRegisterDto dto = RequestRegisterDto.builder()
                .email("test@example.com")
                .password("password")
                .build();

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("hashedPassword");

        ResponseRegisterDto response = authService.register(dto);

        verify(userRepository).save(any());

        assertNotNull(response);
        assertEquals(dto.getEmail(), response.getEmail());
    }

    @Test
    void register_emailAlreadyExists_throwsConflictException() {
        RequestRegisterDto dto = RequestRegisterDto.builder()
                .email("test@example.com")
                .password("password")
                .build();

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class, () -> authService.register(dto));
        assertEquals("already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success() {
        RequestLoginDto dto = RequestLoginDto.builder()
                .email("test@example.com")
                .password("password")
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(dto.getEmail())
                .role(UserRole.USER)
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(dto.getEmail());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user.getEmail(), user.getId(), user.getRole().name()))
                .thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user.getEmail())).thenReturn("refreshToken");
        when(jwtService.getRefreshTokenExpiryDate()).thenReturn(java.util.Date.from(Instant.now()));

        ResponseLoginDto response = authService.login(dto);

        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void login_badCredentials_throwsException() {
        RequestLoginDto dto = RequestLoginDto.builder()
                .email("test@example.com")
                .password("wrongPassword")
                .build();

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThrows(LoginFailedException.class, () -> authService.login(dto));
        verify(refreshTokenRepository, never()).save(any());
    }
}
