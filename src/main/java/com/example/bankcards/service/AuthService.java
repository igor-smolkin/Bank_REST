package com.example.bankcards.service;

import com.example.bankcards.dto.user.login.RequestLoginDto;
import com.example.bankcards.dto.user.login.ResponseLoginDto;
import com.example.bankcards.dto.user.register.RequestRegisterDto;
import com.example.bankcards.dto.user.register.ResponseRegisterDto;
import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.LoginFailedException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.RefreshTokenRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.service.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    public ResponseRegisterDto register (RequestRegisterDto dto) {
        log.info("User with email '{}' trying to register", dto.getEmail());
        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Registration error: user with email '{}' already exists", dto.getEmail());
            throw new ConflictException("already exists");
        }

        String hashedPassword = passwordEncoder.encode(dto.getPassword());

        User user = User.builder()
                .email(dto.getEmail())
                .password(hashedPassword)
                .role(UserRole.USER)
                .createdAt(Instant.now())
                .isEnabled(true)
                .build();

        userRepository.save(user);
        log.info("User with email '{}' registered successfully", user.getEmail());

        return ResponseRegisterDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }

    public ResponseLoginDto login (RequestLoginDto dto) {
        try {
            log.info("User with email '{}' trying to login", dto.getEmail());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getEmail(),
                            dto.getPassword()
                    )
            );

            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> {
                        log.warn("Login error: user with email '{}' not found", dto.getEmail());
                        return new NotFoundException("user not found");
                    });

            String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getId(), user.getRole().name());
            String refreshToken = jwtService.generateRefreshToken(user.getEmail());

            Date expiryDate = jwtService.getRefreshTokenExpiryDate();

            refreshTokenRepository.save(
                    RefreshToken.builder()
                            .user(user)
                            .token(refreshToken)
                            .expiryDate(expiryDate.toInstant())
                            .build()
            );

            log.info("User with email '{}' logged in successfully", user.getEmail());
            return new ResponseLoginDto(accessToken, refreshToken);
        } catch (BadCredentialsException e) {
            log.warn("Login error: user '{}', wrong email or password", dto.getEmail());
            throw new LoginFailedException("bad credentials");
        } catch (DisabledException e) {
            log.warn("Login error: user '{}' is disabled", dto.getEmail());
            throw new LoginFailedException("user disabled");
        }
    }
}
