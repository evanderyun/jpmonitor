package com.jpm.erp.api;

import com.jpm.erp.domains.core.entity.Role;
import com.jpm.erp.domains.core.entity.User;
import com.jpm.erp.domains.core.repository.RoleRepository;
import com.jpm.erp.domains.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@SpringBootApplication
@ComponentScan(basePackages = "com.jpm.erp")
@EntityScan(basePackages = "com.jpm.erp")
@EnableJpaRepositories(basePackages = "com.jpm.erp")
@RequiredArgsConstructor
public class JpmErpApplication {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(JpmErpApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Ensure roles exist
            Optional<Role> adminRoleOpt = roleRepository.findByCode("ROLE_SUPER_ADMIN");
            Role adminRole;
            if (adminRoleOpt.isEmpty()) {
                adminRole = new Role();
                adminRole.setCode("ROLE_SUPER_ADMIN");
                adminRole.setName("Super Administrator");
                adminRole.setDescription("Full System Access");
                // Permissions handled via JSONB default
                adminRole = roleRepository.save(adminRole);
                System.out.println("Seeded ROLE_SUPER_ADMIN");
            } else {
                adminRole = adminRoleOpt.get();
            }

            // Ensure admin user exists and reset password
            Optional<User> adminUserOpt = userRepository.findByUsername("admin");
            User adminUser;
            if (adminUserOpt.isEmpty()) {
                adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setEmail("admin@jpm.local");
                adminUser.setFullName("System Administrator");
                adminUser.setIsActive(true);
                adminUser.setRole(adminRole);
                // Use environment variable or property for admin password.
                // NEVER hardcode production credentials.
                String adminPassword = System.getProperty("ADMIN_PASSWORD");
                if (adminPassword == null || adminPassword.isBlank()) {
                    System.err.println("FATAL: ADMIN_PASSWORD environment variable is not set. Admin account not secured.");
                    // In production, we might want to exit or throw exception.
                    // For now, we set a random UUID to prevent default access.
                    adminPassword = java.util.UUID.randomUUID().toString();
                    System.out.println("Generated temporary admin password: " + adminPassword);
                }
                adminUser.setPasswordHash(passwordEncoder.encode(adminPassword)); // Set using encoder
                userRepository.save(adminUser);
                System.out.println("Seeded/Reset admin user password.");
            }

            // Other default data like Project, Location, CostCategory (V9) will be handled
            // by Flyway
            // This CommandLineRunner primarily ensures the admin user can always log in.
        };
    }
}