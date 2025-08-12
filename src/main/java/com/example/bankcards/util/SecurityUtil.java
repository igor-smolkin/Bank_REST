package com.example.bankcards.util;

import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.security.adapter.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityUtil {

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails user)) {
            throw new UnauthorizedException("User not authorized");
        }
        return user.getUserId();
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails user)) {
            throw new UnauthorizedException("User not authorized");
        }
        return user.getUsername();
    }
}
