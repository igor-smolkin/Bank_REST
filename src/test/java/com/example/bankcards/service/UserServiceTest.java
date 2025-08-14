package com.example.bankcards.service;

import com.example.bankcards.dto.user.select.ResponseUserDto;
import com.example.bankcards.dto.user.update.RequestAdminUpdateUserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void disableUser_success() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .isEnabled(true)
                .build();

        when(securityUtil.getCurrentUsername()).thenReturn("admin@example.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(ResponseUserDto.builder().id(userId).isEnabled(false).build());

        ResponseUserDto response = userService.disableUser(userId);

        assertFalse(user.isEnabled());
        assertEquals(userId, response.getId());
        verify(userRepository).save(user);
    }

    @Test
    void disableUser_alreadyDisabled_throwsConflict() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .isEnabled(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ConflictException exception = assertThrows(ConflictException.class, () -> userService.disableUser(userId));
        assertEquals("user already disabled", exception.getMessage());
    }

    @Test
    void disableUser_notFound_throwsNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.disableUser(userId));
        assertEquals("user not found", exception.getMessage());
    }

    @Test
    void enableUser_success() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .isEnabled(false)
                .build();

        when(securityUtil.getCurrentUsername()).thenReturn("admin@example.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(ResponseUserDto.builder().id(userId).isEnabled(true).build());

        ResponseUserDto response = userService.enableUser(userId);

        assertTrue(user.isEnabled());
        assertEquals(userId, response.getId());
        verify(userRepository).save(user);
    }

    @Test
    void enableUser_alreadyEnabled_throwsConflict() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .isEnabled(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ConflictException exception = assertThrows(ConflictException.class, () -> userService.enableUser(userId));
        assertEquals("user already enabled", exception.getMessage());
    }

    @Test
    void updateUserByAdmin_success() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("old@example.com")
                .role(UserRole.USER)
                .build();

        RequestAdminUpdateUserDto dto = RequestAdminUpdateUserDto.builder()
                .email("new@example.com")
                .role(UserRole.ADMIN)
                .build();

        when(securityUtil.getCurrentUsername()).thenReturn("admin@example.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(ResponseUserDto.builder()
                .id(userId)
                .email("new@example.com")
                .role(UserRole.ADMIN)
                .build());

        ResponseUserDto response = userService.updateUserByAdmin(userId, dto);

        assertEquals("new@example.com", user.getEmail());
        assertEquals(UserRole.ADMIN, user.getRole());
        assertEquals(userId, response.getId());
        verify(userRepository).save(user);
    }

    @Test
    void updateUserByAdmin_notFound_throwsNotFound() {
        UUID userId = UUID.randomUUID();
        RequestAdminUpdateUserDto dto = RequestAdminUpdateUserDto.builder()
                .email("new@example.com")
                .role(UserRole.ADMIN)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.updateUserByAdmin(userId, dto));
        assertEquals("user not found", exception.getMessage());
    }
}
