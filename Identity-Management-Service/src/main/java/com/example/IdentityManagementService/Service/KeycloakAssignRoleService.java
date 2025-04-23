package com.example.IdentityManagementService.Service;

import com.example.IdentityManagementService.exceptions.KeycloakException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.example.common.constants.errorCode.NOT_FOUND_ERROR;
import static com.example.common.constants.errorMessage.ROLE_NOT_FOUND;
import static com.example.common.constants.errorMessage.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAssignRoleService {

    private final Keycloak keycloakAdmin;

    @Value("${keycloak.realm}")
    private String realm;

    public void assignRealmRoles(String username, List<String> roles) {
        RealmResource realmResource = keycloakAdmin.realm(realm);

        // Search for user by username
        List<UserRepresentation> users = realmResource.users().search(username);
        if (users.isEmpty()) {
            throw new KeycloakException(NOT_FOUND_ERROR, USER_NOT_FOUND + username);
        }

        // Assuming username is unique, use the first user found
        UserRepresentation userRepresentation = users.get(0);
        String userId = userRepresentation.getId();

        List<RoleRepresentation> roleRepresentations = new ArrayList<>();
        for (String roleName : roles) {
            try {
                RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
                roleRepresentations.add(role);
            } catch (Exception e) {
                throw new KeycloakException(NOT_FOUND_ERROR, ROLE_NOT_FOUND + roleName);
            }
        }

        // Get the UserResource and assign roles
        UserResource userResource = realmResource.users().get(userId);
        try {
            userResource.roles().realmLevel().add(roleRepresentations);
            log.info("Assigned roles {} to user {}", roles, username);
        } catch (Exception e) {
            throw new KeycloakException(NOT_FOUND_ERROR, "Error assigning roles to user " + username);
        }
    }
}
