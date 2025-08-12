package com.example.bankcards.service;

import com.example.bankcards.dto.user.login.RequestLoginDto;
import com.example.bankcards.dto.user.login.ResponseLoginDto;
import com.example.bankcards.dto.user.register.RequestRegisterDto;
import com.example.bankcards.dto.user.register.ResponseRegisterDto;
import com.example.bankcards.dto.user.select.ResponseUserDto;
import com.example.bankcards.dto.user.update.RequestAdminUpdateUserDto;
import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.RefreshTokenRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.service.JwtService;
import com.example.bankcards.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecurityUtil securityUtil;
    private final UserMapper userMapper;

    @Transactional
    public ResponseRegisterDto register (RequestRegisterDto dto) {
        log.info("try register"); // TODO - нормальные логи
        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("already exists"); // TODO - нормальные логи
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
        log.info("user created"); // TODO - нормальные логи

        return ResponseRegisterDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }

    public ResponseLoginDto login (RequestLoginDto dto) {
        try {
            log.info("try login"); // TODO - нормальные логи
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getEmail(),
                            dto.getPassword()
                    )
            );

            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new NotFoundException("user not found"));

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

            log.info("success login"); // TODO - нормальные логи
            return new ResponseLoginDto(accessToken, refreshToken);
        } catch (BadCredentialsException e) {
            log.warn("wrong login or pass"); // TODO - нормальные логи
            throw e;
        } catch (DisabledException e) {
            log.warn("user disabled"); // TODO - нормальные логи
            throw e;
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseUserDto disableUser(UUID userId) {
        String email = securityUtil.getCurrentUsername();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Disable user error: user with id '{}' not found", userId);
                    return new NotFoundException("user not found");
                });

        if (!user.isEnabled()) {
            throw new ConflictException("user already disabled");
        }

        user.setEnabled(false);
        userRepository.save(user);
        log.info("user '{}' was disabled by admin '{}'", userId, email);

        return userMapper.toDto(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseUserDto enableUser(UUID userId) {
        String email = securityUtil.getCurrentUsername();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Enable user error: user with id '{}' not found", userId);
                    return new NotFoundException("user not found");
                });

        if (user.isEnabled()) {
            throw new ConflictException("user already enabled");
        }

        user.setEnabled(true);
        userRepository.save(user);
        log.info("user '{}' was enabled by admin '{}'", userId, email);

        return userMapper.toDto(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseUserDto updateUserByAdmin(UUID userId, RequestAdminUpdateUserDto dto) {
        String email = securityUtil.getCurrentUsername();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Updating user error: user with id '{}' not found", userId);
                    return new NotFoundException("user not found");
                });

        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getRole() != null) user.setRole(dto.getRole());

        userRepository.save(user);

        log.info("user '{}' successfully updated by admin '{}'", userId, email);

        return userMapper.toDto(user);
    }
}
