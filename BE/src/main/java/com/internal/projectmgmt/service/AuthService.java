package com.internal.projectmgmt.service;

import com.internal.projectmgmt.dto.auth.LoginRequest;
import com.internal.projectmgmt.dto.auth.LoginResponse;
import com.internal.projectmgmt.entity.AppUser;
import com.internal.projectmgmt.exception.AppException;
import com.internal.projectmgmt.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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

        String token = jwtService.generateToken(user);
        OffsetDateTime expiresAt = OffsetDateTime.now()
                .plusSeconds(jwtService.extractExpiration(token).getTime() / 1000
                        - System.currentTimeMillis() / 1000);

        java.util.List<String> permissions = user.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());

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
