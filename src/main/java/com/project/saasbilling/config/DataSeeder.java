package com.project.saasbilling.config;

import com.project.saasbilling.model.Role;
import com.project.saasbilling.model.User;
import com.project.saasbilling.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Seeds initial data on application startup.
 * Creates a default admin user if one doesn't exist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin123!}")
    private String adminPassword;

    @Value("${app.admin.name:System Admin}")
    private String adminName;

    @Value("${app.seed-data:true}")
    private boolean seedData;

    @Override
    public void run(String... args) {
        if (!seedData) {
            log.info("Data seeding is disabled");
            return;
        }

        seedAdminUser();
    }

    private void seedAdminUser() {
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            log.info("Admin user already exists: {}", adminEmail);
            return;
        }

        User admin = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .fullName(adminName)
                .role(Role.ADMIN)
                .active(true)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(admin);
        log.info("Created default admin user: {} / {}", adminEmail, adminPassword);
    }
}
