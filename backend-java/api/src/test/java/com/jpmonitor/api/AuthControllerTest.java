package com.jpmonitor.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmonitor.api.controller.AuthController;
import com.jpmonitor.domains.core.dto.AuthResponse;
import com.jpmonitor.domains.core.dto.LoginRequest;
import com.jpmonitor.domains.core.dto.UserDTO;
import com.jpmonitor.domains.core.entity.Role;
import com.jpmonitor.domains.core.entity.User;
import com.jpmonitor.domains.core.repository.UserRepository;
import com.jpmonitor.platform.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Controller Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private UserRepository userRepository;

    private User testUser;
    private Role testRole;
    private UserDTO testUserDTO;
    private static final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiJ9.test-token";
    private static final String TEST_USERNAME = "admin";
    private static final String TEST_PASSWORD = "admin123";

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(UUID.randomUUID());
        testRole.setCode("ADMIN");
        testRole.setName("Administrator");
        testRole.setPermissions("[\"*\"]");

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername(TEST_USERNAME);
        testUser.setEmail("admin@jpmonitor.com");
        testUser.setFullName("Admin User");
        testUser.setPasswordHash("hash");
        testUser.setRole(testRole);
        testUser.setIsActive(true);

        testUserDTO = new UserDTO(
                testUser.getId(),
                testUser.getUsername(),
                testUser.getEmail(),
                testUser.getFullName(),
                testRole.getName(),
                List.of("*")
        );
    }

    @Test
    @DisplayName("POST /api/auth/login with valid credentials returns JWT token and user info")
    void testLoginWithValidCredentials() throws Exception {
        // Given
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(TEST_USERNAME);
        when(userDetails.getPassword()).thenReturn("hash");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername(TEST_USERNAME))
                .thenReturn(Optional.of(testUser));
        when(jwtUtils.generateToken(userDetails))
                .thenReturn(TEST_TOKEN);

        LoginRequest loginRequest = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(TEST_TOKEN))
                .andExpect(jsonPath("$.user.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.user.email").value("admin@jpmonitor.com"))
                .andExpect(jsonPath("$.user.fullName").value("Admin User"))
                .andExpect(jsonPath("$.user.role").value("Administrator"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(jwtUtils).generateToken(userDetails);
    }

    @Test
    @DisplayName("POST /api/auth/login with invalid credentials returns 401")
    void testLoginWithInvalidCredentials() throws Exception {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        LoginRequest loginRequest = new LoginRequest("wronguser", "wrongpass");

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtUtils);
    }

    @Test
    @DisplayName("GET /api/auth/me with valid token returns current user info")
    void testGetCurrentUserWithValidToken() throws Exception {
        // Given
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(TEST_USERNAME);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(TEST_USERNAME);

        when(userRepository.findByUsername(TEST_USERNAME))
                .thenReturn(Optional.of(testUser));

        // When/Then - we simulate a valid token by mocking the authentication
        // Spring Security will create the Authentication for us if the token is valid
        // Here we're testing the controller logic with a pre-authenticated principal
        mockMvc.perform(get("/api/auth/me")
                        .principal(authentication)
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.email").value("admin@jpmonitor.com"))
                .andExpect(jsonPath("$.fullName").value("Admin User"))
                .andExpect(jsonPath("$.role").value("Administrator"));

        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("GET /api/auth/me without authentication returns 401")
    void testGetCurrentUserWithoutAuth() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}