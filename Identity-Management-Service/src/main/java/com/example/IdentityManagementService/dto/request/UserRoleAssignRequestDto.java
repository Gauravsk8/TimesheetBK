package com.example.IdentityManagementService.dto.request;


import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserRoleAssignRequestDto {

    @NotEmpty(message = "Username is required")
    private String username;

    @NotEmpty(message = "At least one role must be provided")
    private List<String> roles;
}


