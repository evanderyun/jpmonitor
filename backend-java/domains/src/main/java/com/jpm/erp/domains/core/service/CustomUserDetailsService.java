package com.jpm.erp.domains.core.service;

import com.jpm.erp.domains.core.entity.User;
import com.jpm.erp.domains.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("CustomUserDetailsService: Loading user " + username);
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            System.out.println("CustomUserDetailsService: User found: " + user.getId());
            System.out.println("CustomUserDetailsService: Role lookup...");
            String roleCode = user.getRole().getCode();
            System.out.println("CustomUserDetailsService: Role code: " + roleCode);

            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPasswordHash(),
                    user.getIsActive() != null && user.getIsActive(),
                    true,
                    true,
                    true,
                    Collections.singletonList(new SimpleGrantedAuthority(roleCode)));
        } catch (Exception e) {
            System.err.println("CustomUserDetailsService: ERROR loading user " + username);
            e.printStackTrace();
            throw e;
        }
    }
}