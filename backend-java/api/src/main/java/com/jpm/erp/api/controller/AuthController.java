package com.jpm.erp.api.controller;

import com.jpm.erp.domains.core.dto.AuthResponse;
import com.jpm.erp.domains.core.dto.LoginRequest;
import com.jpm.erp.domains.core.dto.UserDTO;
import com.jpm.erp.domains.core.entity.User;
import com.jpm.erp.domains.core.repository.UserRepository;
import com.jpm.erp.platform.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthenticationManager authenticationManager;
        private final JwtUtils jwtUtils;
        private final UserRepository userRepository;

        @PostMapping("/login")
        @Transactional(readOnly = true)
        public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
                try {
                        System.out.println("=== LOGIN ATTEMPT ===");
                        System.out.println("Username: " + request.username());

                        Authentication authentication = authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(request.username(),
                                                        request.password()));
                        System.out.println("Authentication successful");

                        org.springframework.security.core.userdetails.UserDetails userDetails = (org.springframework.security.core.userdetails.UserDetails) authentication
                                        .getPrincipal();
                        System.out.println("UserDetails retrieved");

                        User user = userRepository.findByUsername(request.username()).orElseThrow();
                        System.out.println("User loaded from DB: " + user.getUsername());
                        System.out.println(
                                        "User role: " + (user.getRole() != null ? user.getRole().getCode() : "NULL"));

                        String token = jwtUtils.generateToken(userDetails);
                        System.out.println("Token generated");

                        UserDTO userDTO = new UserDTO(
                                        user.getId(),
                                        user.getUsername(),
                                        user.getEmail(),
                                        user.getFullName(),
                                        user.getRole().getName(),
                                        user.getRole().getPermissions());
                        System.out.println("UserDTO created");

                        return ResponseEntity.ok(new AuthResponse(token, userDTO));
                } catch (Exception e) {
                        System.err.println("=== LOGIN ERROR ===");
                        e.printStackTrace();
                        throw e;
                }
        }

        @GetMapping("/me")
        @Transactional(readOnly = true)
        public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
                if (authentication == null)
                        return ResponseEntity.status(401).build();
                User user = userRepository.findByUsername(authentication.getName()).orElseThrow();

                UserDTO userDTO = new UserDTO(
                                user.getId(),
                                user.getUsername(),
                                user.getEmail(),
                                user.getFullName(),
                                user.getRole().getName(),
                                user.getRole().getPermissions());
                return ResponseEntity.ok(userDTO);
        }
}
