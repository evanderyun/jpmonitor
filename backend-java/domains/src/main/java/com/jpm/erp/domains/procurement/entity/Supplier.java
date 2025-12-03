package com.jpm.erp.domains.procurement.entity;

import com.jpm.erp.platform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "suppliers")
public class Supplier extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "contact_person")
    private String contactPerson;

    private String phone;
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    private Integer rating;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "sla_response_time_hours")
    private Integer slaResponseTimeHours;

    @Column(name = "api_integration_enabled")
    private Boolean apiIntegrationEnabled = false;

    @Column(name = "api_endpoint_url")
    private String apiEndpointUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "api_auth_config", columnDefinition = "jsonb")
    private Map<String, Object> apiAuthConfig;
}
