package com.internal.projectmgmt.controller;

import com.internal.projectmgmt.dto.ApiResponse;
import com.internal.projectmgmt.dto.auth.LoginRequest;
import com.internal.projectmgmt.dto.auth.LoginResponse;
import com.internal.projectmgmt.entity.AppUser;
import com.internal.projectmgmt.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // Stateless — client drops token; no server-side action needed
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponse.UserInfo>> me(
            @AuthenticationPrincipal AppUser user,
            Authentication authentication) {
        java.util.List<String> permissions = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());

        LoginResponse.UserInfo info = new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole().getId(),
                user.getRole().getRoleName(),
                permissions);
        return ResponseEntity.ok(ApiResponse.success(info));
    }
}
