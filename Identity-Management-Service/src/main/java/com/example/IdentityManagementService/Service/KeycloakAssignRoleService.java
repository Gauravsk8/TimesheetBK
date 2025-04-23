package com.example.IdentityManagementService.Service;

import com.example.IdentityManagementService.exceptions.KeycloakException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

import static com.example.common.constants.errorCode.NOT_FOUND_ERROR;
import static com.example.common.constants.errorMessage.ROLE_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAssignRoleService {

    private final Keycloak keycloakAdmin;

    @Value("${keycloak.realm}")
    private String realm;

    public void assignRealmRoles(String userId, List<String> roles) {
        RealmResource realmResource = keycloakAdmin.realm(realm);
        List<RoleRepresentation> roleRepresentations = new ArrayList<>();

        for (String roleName : roles) {
            try {
                RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
                roleRepresentations.add(role);
            } catch (Exception e) {
                throw new KeycloakException(NOT_FOUND_ERROR, ROLE_NOT_FOUND + roleName);
            }
        }

        UserResource userResource = realmResource.users().get(userId);
        userResource.roles().realmLevel().add(roleRepresentations);

        log.info("Assigned roles {} to user {}", roles, userId);
    }
}
