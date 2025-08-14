package com.example.bankcards.security.config;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDefaultUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setEmail("admin");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setRole(UserRole.ADMIN);
                admin.setEnabled(true);
                admin.setCreatedAt(Instant.now());
                userRepository.save(admin);

                User user = new User();
                user.setEmail("user");
                user.setPassword(passwordEncoder.encode("user"));
                user.setRole(UserRole.USER);
                user.setEnabled(true);
                admin.setCreatedAt(Instant.now());
                userRepository.save(user);
            }
        };
    }
}
