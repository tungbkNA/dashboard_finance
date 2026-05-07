package com.internal.projectmgmt.service;

import com.internal.projectmgmt.dto.auth.LoginRequest;
import com.internal.projectmgmt.dto.auth.LoginResponse;
import com.internal.projectmgmt.entity.AppUser;
import com.internal.projectmgmt.entity.Permission;
import com.internal.projectmgmt.entity.Role;
import com.internal.projectmgmt.exception.AppException;
import com.internal.projectmgmt.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Role activeRole;
    private Role inactiveRole;
    private AppUser activeUser;

    @BeforeEach
    void setUp() {
        Permission perm = Permission.builder()
                .code("VIEW_DASHBOARD")
                .displayName("Xem Dashboard")
                .type(Permission.PermissionType.SCREEN)
                .build();

        activeRole = Role.builder()
                .id(UUID.randomUUID())
                .roleName("SYSTEM_ADMIN")
                .active(true)
                .deleted(false)
                .permissions(Set.of(perm))
                .build();

        inactiveRole = Role.builder()
                .id(UUID.randomUUID())
                .roleName("OLD_ROLE")
                .active(false)
                .deleted(false)
                .permissions(Set.of())
                .build();

        activeUser = AppUser.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .email("admin@internal.com")
                .displayName("Admin")
                .passwordHash("$2a$10$hashedpassword")
                .role(activeRole)
                .active(true)
                .deleted(false)
                .build();
    }

    @Test
    void login_withCorrectCredentials_shouldReturnLoginResponse() {
        when(appUserRepository.findByUsernameAndDeletedFalse("admin")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password123", activeUser.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(activeUser)).thenReturn("mock.jwt.token");
        when(jwtService.extractExpiration("mock.jwt.token"))
                .thenReturn(new java.util.Date(System.currentTimeMillis() + 8 * 3600 * 1000));

        LoginResponse response = authService.login(new LoginRequest("admin", "password123"));

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("mock.jwt.token");
        assertThat(response.user().username()).isEqualTo("admin");
    }

    @Test
    void login_withWrongPassword_shouldThrowAuthInvalidCredentials() {
        when(appUserRepository.findByUsernameAndDeletedFalse("admin")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrongpass", activeUser.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "wrongpass")))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("không đúng")
                .satisfies(e -> assertThat(((AppException) e).getCode()).isEqualTo("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    void login_withUnknownUsername_shouldThrowAuthInvalidCredentials() {
        when(appUserRepository.findByUsernameAndDeletedFalse("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody", "pass")))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getCode()).isEqualTo("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    void login_withInactiveUser_shouldThrowAuthAccountInactive() {
        AppUser inactiveUser = AppUser.builder()
                .id(UUID.randomUUID())
                .username("inactive")
                .passwordHash("hash")
                .role(activeRole)
                .active(false)
                .deleted(false)
                .build();
        when(appUserRepository.findByUsernameAndDeletedFalse("inactive")).thenReturn(Optional.of(inactiveUser));

        assertThatThrownBy(() -> authService.login(new LoginRequest("inactive", "pass")))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getCode()).isEqualTo("AUTH_ACCOUNT_INACTIVE"));
    }

    @Test
    void login_withInactiveRole_shouldThrowAuthRoleInactive() {
        AppUser userWithInactiveRole = AppUser.builder()
                .id(UUID.randomUUID())
                .username("roleuser")
                .passwordHash("hash")
                .role(inactiveRole)
                .active(true)
                .deleted(false)
                .build();
        when(appUserRepository.findByUsernameAndDeletedFalse("roleuser")).thenReturn(Optional.of(userWithInactiveRole));

        assertThatThrownBy(() -> authService.login(new LoginRequest("roleuser", "pass")))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getCode()).isEqualTo("AUTH_ROLE_INACTIVE"));
    }
}
