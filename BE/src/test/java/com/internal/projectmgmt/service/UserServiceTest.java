package com.internal.projectmgmt.service;

import com.internal.projectmgmt.dto.user.UserRequest;
import com.internal.projectmgmt.entity.AppUser;
import com.internal.projectmgmt.entity.Role;
import com.internal.projectmgmt.exception.AppException;
import com.internal.projectmgmt.mapper.UserMapper;
import com.internal.projectmgmt.repository.AppUserRepository;
import com.internal.projectmgmt.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private Role activeRole;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();
        activeRole = Role.builder()
                .id(roleId)
                .roleName("SYSTEM_ADMIN")
                .active(true)
                .deleted(false)
                .permissions(Set.of())
                .build();
    }

    @Test
    void create_shouldBCryptHashPassword() {
        UserRequest req = new UserRequest("newuser", "user@test.com", "New User", "plaintext1", roleId);
        when(appUserRepository.existsByUsernameAndDeletedFalse("newuser")).thenReturn(false);
        when(appUserRepository.existsByEmailAndDeletedFalse("user@test.com")).thenReturn(false);
        when(roleRepository.findByIdAndDeletedFalse(roleId)).thenReturn(Optional.of(activeRole));
        when(passwordEncoder.encode("plaintext1")).thenReturn("$2a$10$hashedpassword");
        when(appUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any())).thenAnswer(inv -> {
            AppUser u = inv.getArgument(0);
            return new com.internal.projectmgmt.dto.user.UserResponse(u.getId(), u.getUsername(),
                    u.getEmail(), u.getDisplayName(), u.getRole().getId(),
                    u.getRole().getRoleName(), u.isActive(), u.getCreatedAt());
        });

        userService.create(req);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash())
                .isEqualTo("$2a$10$hashedpassword")
                .doesNotContain("plaintext1");
    }

    @Test
    void create_withDuplicateUsername_shouldThrow() {
        when(appUserRepository.existsByUsernameAndDeletedFalse("dup")).thenReturn(true);
        UserRequest req = new UserRequest("dup", "dup@test.com", "Dup", "password1", roleId);
        assertThatThrownBy(() -> userService.create(req))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getCode()).isEqualTo("USER_USERNAME_EXISTS"));
    }

    @Test
    void create_withDuplicateEmail_shouldThrow() {
        when(appUserRepository.existsByUsernameAndDeletedFalse("newuser")).thenReturn(false);
        when(appUserRepository.existsByEmailAndDeletedFalse("dup@test.com")).thenReturn(true);
        UserRequest req = new UserRequest("newuser", "dup@test.com", "Dup", "password1", roleId);
        assertThatThrownBy(() -> userService.create(req))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getCode()).isEqualTo("USER_EMAIL_EXISTS"));
    }

    @Test
    void create_withInactiveRole_shouldThrow() {
        Role inactiveRole = Role.builder().id(roleId).roleName("OLD").active(false).deleted(false).permissions(Set.of())
                .build();
        when(appUserRepository.existsByUsernameAndDeletedFalse("newuser")).thenReturn(false);
        when(appUserRepository.existsByEmailAndDeletedFalse("user@test.com")).thenReturn(false);
        when(roleRepository.findByIdAndDeletedFalse(roleId)).thenReturn(Optional.of(inactiveRole));
        UserRequest req = new UserRequest("newuser", "user@test.com", "New", "password1", roleId);
        assertThatThrownBy(() -> userService.create(req))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getCode()).isEqualTo("ROLE_NOT_FOUND_OR_INACTIVE"));
    }

    @Test
    void softDelete_self_shouldThrow() {
        UUID userId = UUID.randomUUID();
        assertThatThrownBy(() -> userService.softDelete(userId, userId))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getCode()).isEqualTo("USER_CANNOT_DELETE_SELF"));
    }
}
