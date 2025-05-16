package com.example.IdentityManagementService.model;

import com.example.common.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @Column(name = "employee_code", nullable = false)
    private String employeeCode;

    @Column(name = "keycloak_user_id")
    private String keycloakUserId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "employee_type")
    private String employeeType;

    @Column(name = "manager_code")
    private String managerCode;

    @Column(name = "is_active")
    private boolean isActive;
}
