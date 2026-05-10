package com.internal.projectmgmt.service;

import com.internal.projectmgmt.dto.auth.LoginRequest;
import com.internal.projectmgmt.dto.auth.LoginResponse;
import com.internal.projectmgmt.entity.AppUser;
import com.internal.projectmgmt.exception.AppException;
import com.internal.projectmgmt.repository.AppUserRepository;
import com.internal.projectmgmt.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String ADMIN_USERNAME = "admin";

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // Load user — generic error to avoid username enumeration
        AppUser user = appUserRepository.findByUsernameAndDeletedFalse(request.username())
                .orElseThrow(
                        () -> new AppException("AUTH_INVALID_CREDENTIALS", "Tên đăng nhập hoặc mật khẩu không đúng"));

        // Check account active
        if (!user.isActive()) {
            throw new AppException("AUTH_ACCOUNT_INACTIVE", "Tài khoản bị vô hiệu hoá");
        }

        // Check role active
        if (!user.getRole().isActive()) {
            throw new AppException("AUTH_ROLE_INACTIVE", "Nhóm quyền của tài khoản bị vô hiệu hoá");
        }

        // Verify password
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AppException("AUTH_INVALID_CREDENTIALS", "Tên đăng nhập hoặc mật khẩu không đúng");
        }

        // Admin gets ALL permissions; other users get role-based permissions
        List<String> permissions;
        String token;
        if (ADMIN_USERNAME.equals(user.getUsername())) {
            permissions = permissionRepository.findAllCodes();
            token = jwtService.generateToken(user, permissions);
        } else {
            permissions = user.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .collect(Collectors.toList());
            token = jwtService.generateToken(user);
        }

        OffsetDateTime expiresAt = OffsetDateTime.now()
                .plusSeconds(jwtService.extractExpiration(token).getTime() / 1000
                        - System.currentTimeMillis() / 1000);

        return new LoginResponse(
                token,
                expiresAt,
                new LoginResponse.UserInfo(
                        user.getId(),
                        user.getUsername(),
                        user.getDisplayName(),
                        user.getRole().getId(),
                        user.getRole().getRoleName(),
                        permissions));
    }
}
