package com.internal.projectmgmt.service;

import com.internal.projectmgmt.entity.AppUser;
import com.internal.projectmgmt.entity.Permission;
import com.internal.projectmgmt.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;
    private AppUser testUser;

    @BeforeEach
    void setUp() {
        // Base64 of a 32-byte key for HMAC-SHA256
        String secret = "dGhpc2lzYXZlcnlsb25nYW5kc2VjdXJlc2VjcmV0a2V5Zm9yamRhc2hib2FyZA==";
        jwtService = new JwtService(secret, 8);

        Permission perm = Permission.builder()
                .code("VIEW_DASHBOARD")
                .displayName("Xem Dashboard")
                .type(Permission.PermissionType.SCREEN)
                .build();

        Role role = Role.builder()
                .id(UUID.randomUUID())
                .roleName("SYSTEM_ADMIN")
                .active(true)
                .deleted(false)
                .permissions(Set.of(perm))
                .build();

        testUser = AppUser.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .email("admin@internal.com")
                .displayName("Administrator")
                .passwordHash("irrelevant")
                .role(role)
                .active(true)
                .deleted(false)
                .build();
    }

    @Test
    void generateToken_thenValidate_shouldReturnTrue() {
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.validateToken(token)).isTrue();
    }

    @Test
    void generateToken_thenExtractUsername_shouldMatchUser() {
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
    }

    @Test
    void generateToken_thenExtractPermissions_shouldContainUserPermissions() {
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.extractPermissions(token)).contains("VIEW_DASHBOARD");
    }

    @Test
    void tamperedToken_shouldFailValidation() {
        String token = jwtService.generateToken(testUser);
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(jwtService.validateToken(tampered)).isFalse();
    }

    @Test
    void invalidToken_shouldFailValidation() {
        assertThat(jwtService.validateToken("not.a.jwt")).isFalse();
    }

    @Test
    void emptyToken_shouldFailValidation() {
        assertThat(jwtService.validateToken("")).isFalse();
    }
}
