package com.jpm.erp.api;

import com.jpm.erp.domains.core.entity.Role;
import com.jpm.erp.domains.core.entity.User;
import com.jpm.erp.domains.core.repository.RoleRepository;
import com.jpm.erp.domains.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Optional;
import java.util.Set;

@SpringBootApplication
@ComponentScan(basePackages = "com.jpm.erp")
@EntityScan(basePackages = "com.jpm.erp")
@EnableJpaRepositories(basePackages = "com.jpm.erp")
@RequiredArgsConstructor
public class JpmErpApplication {

    private static final Logger log = LoggerFactory.getLogger(JpmErpApplication.class);

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
                adminRole = roleRepository.save(adminRole);
                log.info("Seeded ROLE_SUPER_ADMIN");
            } else {
                adminRole = adminRoleOpt.get();
            }

            // Ensure admin user exists with secure password
            Optional<User> adminUserOpt = userRepository.findByUsername("admin");
            if (adminUserOpt.isEmpty()) {
                String adminPassword = System.getenv("ADMIN_PASSWORD");
                if (adminPassword == null || adminPassword.isBlank()) {
                    adminPassword = java.util.UUID.randomUUID().toString().substring(0, 16);
                }

                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setEmail("admin@jpm.local");
                adminUser.setFullName("System Administrator");
                adminUser.setIsActive(true);
                adminUser.setRole(adminRole);
                adminUser.setPasswordHash(passwordEncoder.encode(adminPassword));
                userRepository.save(adminUser);

                // Write password to secure file (only if no env var was set)
                if (System.getenv("ADMIN_PASSWORD") == null) {
                    Path passwordFile = Paths.get("/opt/jpm-erp/.admin-password");
                    Files.writeString(passwordFile, 
                        "Initial admin password: " + adminPassword + "\n" +
                        "CHANGE THIS PASSWORD IMMEDIATELY after first login.\n" +
                        "Generated: " + java.time.Instant.now() + "\n"
                    );
                    // Restrict permissions to owner only
                    try {
                        Files.setPosixFilePermissions(passwordFile, 
                            Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
                    } catch (UnsupportedOperationException e) {
                        // Not a POSIX filesystem, skip
                    }
                    log.warn("Initial admin password written to /opt/jpm-erp/.admin-password");
                    log.warn("SECURITY: Delete this file after first login: rm /opt/jpm-erp/.admin-password");
                }

                log.info("Seeded admin user. Password must be changed on first login.");
            }
        };
    }
}
