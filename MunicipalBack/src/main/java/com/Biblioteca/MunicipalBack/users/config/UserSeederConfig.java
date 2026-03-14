package com.Biblioteca.MunicipalBack.users.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.Biblioteca.MunicipalBack.shared.enums.UserRole;
import com.Biblioteca.MunicipalBack.users.model.User;
import com.Biblioteca.MunicipalBack.users.repository.UserRepository;

@Configuration
@RequiredArgsConstructor
public class UserSeederConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedAdminUser() {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("Admin123*"))
                        .role(UserRole.ADMIN)
                        .build();

                userRepository.save(admin);
            }
        };
    }
}
