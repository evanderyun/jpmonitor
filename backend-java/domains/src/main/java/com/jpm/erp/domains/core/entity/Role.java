package com.jpm.erp.domains.core.entity;

import com.jpm.erp.platform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Temporarily ignore permissions to get login working
    // TODO: Implement proper JSONB handling later
    @Transient
    private List<String> cachedPermissions;

    // Manual Getters
    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public List<String> getPermissions() {
        // For now, return all permissions for SUPER_ADMIN, empty for others
        // This is a temporary workaround to get login working
        if ("ROLE_SUPER_ADMIN".equals(code)) {
            return Arrays.asList("*");
        }
        return new ArrayList<>();
    }
}