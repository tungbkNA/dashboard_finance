package com.internal.projectmgmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.projectmgmt.config.SecurityConfig;
import com.internal.projectmgmt.dto.auth.LoginRequest;
import com.internal.projectmgmt.dto.auth.LoginResponse;
import com.internal.projectmgmt.entity.AppUser;
import com.internal.projectmgmt.entity.Role;
import com.internal.projectmgmt.service.AuthService;
import com.internal.projectmgmt.service.JwtService;
import com.internal.projectmgmt.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserRepository appUserRepository;

    @Test
    void login_withValidCredentials_returnsToken() throws Exception {
        LoginRequest req = new LoginRequest("admin", "Admin@123456");
        LoginResponse resp = new LoginResponse(
                "jwt-token-here",
                OffsetDateTime.now().plusHours(8),
                new LoginResponse.UserInfo(UUID.randomUUID(), "admin", "Administrator",
                        UUID.randomUUID(), "SYSTEM_ADMIN", List.of("VIEW_DASHBOARD")));
        when(authService.login(any())).thenReturn(resp);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("jwt-token-here"));
    }

    @Test
    void login_withBlankUsername_returnsBadRequest() throws Exception {
        LoginRequest req = new LoginRequest("", "password");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void me_whenAuthenticated_returnsUser() throws Exception {
        Role role = Role.builder().id(UUID.randomUUID()).roleName("SYSTEM_ADMIN")
                .active(true).deleted(false).permissions(Set.of()).build();
        AppUser appUser = AppUser.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .displayName("Administrator")
                .email("admin@test.com")
                .passwordHash("hash")
                .role(role)
                .active(true)
                .deleted(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        mockMvc.perform(get("/api/auth/me").with(user(appUser)))
                .andExpect(status().isOk());
    }

    @Test
    void me_whenNotAuthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
