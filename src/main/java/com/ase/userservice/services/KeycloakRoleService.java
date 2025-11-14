package com.ase.userservice.services;

import com.ase.userservice.controllers.dto.*;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.ws.rs.NotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Locale;

@Service
public class KeycloakRoleService {

    // Hardcodierte Liste, wie im Frontend-Mock
    private static final List<String> STANDARD_ROLES = List.of("Administrator", "PR", "Student", "Dozent", "Redakteur");

    private final RealmResource realm;

    public KeycloakRoleService(RealmResource realm) {
      this.realm = realm;
    }

    public RealmResource getRealm() {
      return realm;
    }

    public List<RoleDto> findAllRoles() {
        return getRealm().roles().list().stream()
                .map(role -> {
                    long userCount = getRealm().roles().get(role.getName()).getRoleUserMembers().size();
                    // Logik für "standardRole" - hier vereinfacht
                    String standardRole = STANDARD_ROLES.contains(role.getName()) ? role.getName() : "";
                    return new RoleDto(role.getId(), role.getName(), userCount);
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

    public List<RoleDto> searchRolesByName(String name) {
      String query = name == null ? "" : name.toLowerCase(Locale.ROOT);
      return findAllRoles().stream()
          .filter(r -> r.name().toLowerCase(Locale.ROOT).contains(query))
          .toList();
    }

    public List<PermissionDto> searchPermissionsByName(String name) {
      String query = name == null ? "" : name.toLowerCase(Locale.ROOT);
      return findAllPermissions().stream()
          .filter(p -> p.name().toLowerCase(Locale.ROOT).contains(query))
          .toList();
    }

  public PermissionDto findPermissionById(String permissionId) {
    var role = getRealm().rolesById().getRole(permissionId);
    return new PermissionDto(role.getId(), role.getName());
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

  // ======  User <-> Permission (Realm-Rolle) ======
  public void addPermissionToUser(String userId, String permissionId) {
    RoleRepresentation role = getRealm().rolesById().getRole(permissionId);
    if (role == null) throw new NotFoundException("Permission not found: " + permissionId);
    getRealm()
        .users()
        .get(userId)
        .roles()
        .realmLevel()
        .add(Collections.singletonList(role));
  }
  public void removePermissionFromUser(String userId, String permissionId) {
    RoleRepresentation role = getRealm().rolesById().getRole(permissionId);
    if (role == null) throw new NotFoundException("Permission not found: " + permissionId);
    getRealm()
        .users()
        .get(userId)
        .roles()
        .realmLevel()
        .remove(Collections.singletonList(role));
  }
}
