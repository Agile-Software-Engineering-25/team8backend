package com.example.keycloak.backend.service;

import com.example.keycloak.backend.dto.*;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KeycloakRoleService {

    private final Keycloak keycloak;
    private final String realm;

    // Hardcodierte Liste, wie im Frontend-Mock
    private static final List<String> STANDARD_ROLES = List.of("Administrator", "PR", "Student", "Dozent", "Redakteur");

    public KeycloakRoleService(Keycloak keycloak, @Value("${keycloak.realm}") String realm) {
        this.keycloak = keycloak;
        this.realm = realm;
    }

    private RealmResource getRealm() {
        return keycloak.realm(realm);
    }

    public List<RoleDto> findAllRoles() {
        return getRealm().roles().list().stream()
                .map(role -> {
                    long userCount = getRealm().roles().get(role.getName()).getRoleUserMembers().size();
                    // Logik für "standardRole" - hier vereinfacht
                    String standardRole = STANDARD_ROLES.contains(role.getName()) ? role.getName() : "";
                    return new RoleDto(role.getId(), role.getName(), standardRole, userCount);
                })
                .collect(Collectors.toList());
    }

    public RoleDetailDto findRoleById(String roleId) {
        RoleRepresentation role = getRealm().rolesById().getRole(roleId);
        List<String> assignedPermissionIds = getRealm().rolesById().getRoleComposites(roleId)
                .stream()
                .map(RoleRepresentation::getId)
                .collect(Collectors.toList());
        return new RoleDetailDto(role.getId(), role.getName(), assignedPermissionIds);
    }
    
    public List<PermissionDto> findAllPermissions() {
        // "Verfügbare Berechtigungen" sind hier als alle anderen Realm-Rollen definiert.
        // In einer echten Anwendung könnten dies auch Client-Rollen sein.
        return getRealm().roles().list().stream()
                .map(role -> new PermissionDto(role.getId(), role.getName()))
                .collect(Collectors.toList());
    }

    public void createRole(CreateRoleRequest request) {
        RoleRepresentation newRole = new RoleRepresentation();
        newRole.setName(request.name());
        newRole.setComposite(true); // Wichtig, um andere Rollen hinzufügen zu können
        getRealm().roles().create(newRole);
    }

    public void updateRole(String roleId, UpdateRoleRequest request) {
        RoleRepresentation role = getRealm().rolesById().getRole(roleId);
        role.setName(request.name());
        getRealm().rolesById().updateRole(roleId, role);
    }

    public void addPermissionToRole(String roleId, String permissionId) {
        RoleRepresentation permissionRole = getRealm().rolesById().getRole(permissionId);
        getRealm().rolesById().addComposites(roleId, Collections.singletonList(permissionRole));
    }

    public void removePermissionFromRole(String roleId, String permissionId) {
        RoleRepresentation permissionRole = getRealm().rolesById().getRole(permissionId);
        getRealm().rolesById().deleteComposites(roleId, Collections.singletonList(permissionRole));
    }
    
    public List<String> getStandardRoles() {
        return STANDARD_ROLES;
    }
}
