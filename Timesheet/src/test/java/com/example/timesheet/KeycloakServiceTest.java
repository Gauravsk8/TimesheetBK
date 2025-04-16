package com.example.timesheet;


import com.example.timesheet.constants.errorCode;
import com.example.timesheet.dto.request.EmployeeRequestDto;
import com.example.timesheet.exceptions.TimeSheetException;
import com.example.timesheet.service.KeycloakService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class KeycloakServiceTest {
    @Mock
    private Keycloak keycloak;
    @Mock
    private RealmResource realmResource;
    @Mock
    private RealmsResource realmsResource;
    @Mock
    private UsersResource usersResource;
    @Mock
    private RolesResource rolesResource;
    @Mock
    private RoleResource roleResource;
    @Mock
    private UserResource userResource;
    @Mock
    private RoleMappingResource roleMappingResource;
    @Mock
    private RoleScopeResource roleScopeResource;
    @Mock
    private Response mockResponse;
    @InjectMocks
    private KeycloakService keycloakService;
    private final String realm = "test-realm";
    @BeforeEach
    void setup() throws Exception {
        setField(keycloakService, "realm", realm);
        setField(keycloakService, "clientId", "test-client");
        when(keycloak.realm(realm)).thenReturn(realmResource);
        when(keycloak.realms()).thenReturn(realmsResource);
        when(realmsResource.findAll()).thenReturn(Collections.emptyList());
        when(realmResource.users()).thenReturn(usersResource);
        when(realmResource.roles()).thenReturn(rolesResource);
    }
    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = KeycloakService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
    @Test
    void createUserWithRole_success() {
        EmployeeRequestDto request = new EmployeeRequestDto();
        request.setEmail("test@example.com");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPassword("secret");
        when(usersResource.search("test@example.com", true)).thenReturn(Collections.emptyList());
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
        when(mockResponse.getLocation()).thenReturn(URI.create("http://localhost/auth/users/abc123"));
        RoleRepresentation role = new RoleRepresentation();
        role.setName("employee");
        when(rolesResource.get("employee")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(role);
        when(usersResource.get("abc123")).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
        String userId = keycloakService.createUserWithRole(request, "employee");
        assertEquals("abc123", userId);
        verify(usersResource).create(any(UserRepresentation.class));
        verify(roleScopeResource).add(List.of(role));
    }
    @Test
    void verifyAdminConnection_shouldSucceed() throws Exception {
        when(keycloak.realms()).thenReturn(realmsResource);
        when(realmsResource.findAll()).thenReturn(Collections.emptyList());
        // Use reflection to call the private method
        var method = KeycloakService.class.getDeclaredMethod("verifyAdminConnection");
        method.setAccessible(true);
        method.invoke(keycloakService); // should not throw
    }
    @Test
    void createUserWithRole_userAlreadyExists_shouldThrowException() {
        EmployeeRequestDto request = new EmployeeRequestDto();
        request.setEmail("duplicate@example.com");
        when(usersResource.search("duplicate@example.com", true))
                .thenReturn(List.of(new UserRepresentation()));
        TimeSheetException ex = assertThrows(
                TimeSheetException.class,
                () -> keycloakService.createUserWithRole(request, "employee")
        );
        assertEquals(errorCode.CONFLICT_ERROR, ex.getErrorCode());
    }
    @Test
    void assignRealmRole_shouldSucceed() throws Exception {
        String userId = "abc123";
        String roleName = "employee";
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        when(rolesResource.get(roleName)).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(role);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
        // Inject mocks into a temp RealmResource mock
        when(realmResource.roles()).thenReturn(rolesResource);
        when(realmResource.users()).thenReturn(usersResource);
        var method = KeycloakService.class.getDeclaredMethod("assignRealmRole", String.class, String.class, RealmResource.class);
        method.setAccessible(true);
        method.invoke(keycloakService, userId, roleName, realmResource);
        verify(roleScopeResource).add(Collections.singletonList(role));
    }
    @Test
    void assignRealmRole_shouldThrowTimesheetException_onError() throws Exception {
        String userId = "abc123";
        String roleName = "employee";
        when(realmResource.roles()).thenThrow(new RuntimeException("Role retrieval failed"));
        var method = KeycloakService.class.getDeclaredMethod("assignRealmRole", String.class, String.class, RealmResource.class);
        method.setAccessible(true);
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () ->
                method.invoke(keycloakService, userId, roleName, realmResource)
        );
        Throwable targetEx = ex.getTargetException();
        assertTrue(targetEx instanceof TimeSheetException);
        assertEquals(errorCode.ROLE_ASSIGNMENT_FAILED, ((TimeSheetException) targetEx).getErrorCode());
    }

    @Test
    void isUserExists_shouldReturnTrue() {
        when(usersResource.search("exists@example.com", true))
                .thenReturn(List.of(new UserRepresentation()));
        assertTrue(keycloakService.isUserExists("exists@example.com"));
    }
    @Test
    void isUserExists_shouldReturnFalse() {
        when(usersResource.search("notfound@example.com", true))
                .thenReturn(Collections.emptyList());
        assertFalse(keycloakService.isUserExists("notfound@example.com"));
    }
}


