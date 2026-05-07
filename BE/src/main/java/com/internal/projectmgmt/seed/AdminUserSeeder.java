package com.internal.projectmgmt.seed;

import com.internal.projectmgmt.entity.AppUser;
import com.internal.projectmgmt.entity.Role;
import com.internal.projectmgmt.repository.AppUserRepository;
import com.internal.projectmgmt.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserSeeder implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.initial-password:}")
    private String initialPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (initialPassword == null || initialPassword.isBlank()) {
            log.warn("AdminUserSeeder: app.admin.initial-password is not set — skipping admin seed");
            return;
        }

        Role systemAdmin = roleRepository.findByRoleNameAndDeletedFalse("SYSTEM_ADMIN")
                .orElse(null);
        if (systemAdmin == null) {
            log.warn("AdminUserSeeder: SYSTEM_ADMIN role not found — skipping admin seed");
            return;
        }

        appUserRepository.findByUsernameAndDeletedFalse("admin").ifPresentOrElse(
                existing -> {
                    // Always sync password and system flag on startup
                    existing.setPasswordHash(passwordEncoder.encode(initialPassword));
                    existing.setSystem(true);
                    existing.setRole(systemAdmin);
                    appUserRepository.save(existing);
                    log.info("AdminUserSeeder: admin user synchronized");
                },
                () -> {
                    AppUser admin = AppUser.builder()
                            .username("admin")
                            .email("admin@internal.com")
                            .displayName("System Administrator")
                            .passwordHash(passwordEncoder.encode(initialPassword))
                            .role(systemAdmin)
                            .active(true)
                            .deleted(false)
                            .system(true)
                            .build();
                    appUserRepository.save(admin);
                    log.info("AdminUserSeeder: admin user created successfully");
                });
    }
}
