package com.ase.userservice.services;

import com.ase.userservice.components.exceptions.BadRequestException;
import com.ase.userservice.components.exceptions.NotFoundException;
import com.ase.userservice.config.RoleMetadataProperties;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoleAssignmentService {

  private final RealmResource realm;
  private final RoleService roleService;
  private final RoleMetadataProperties meta;

  public RoleAssignmentService(RealmResource realm, RoleService roleService, RoleMetadataProperties meta) {
    this.realm = realm;
    this.roleService = roleService;
    this.meta = meta;
  }

  public void assignRoleToUser(String userId, String roleName) {
    RoleRepresentation role = roleService.get(roleName);
    realm.users().get(userId).roles().realmLevel().add(List.of(role));
  }


  public void removeRoleFromUser(String userId, String roleName) {
    RoleRepresentation role = roleService.get(roleName);
    realm.users().get(userId).roles().realmLevel().remove(List.of(role));
    ensureFallbackIfNoStandard(userId);
  }

  public int bulkAssign(String roleName, List<String> userIds) {
    RoleRepresentation role = roleService.get(roleName);
// Validate users first (all-or-nothing)
    for (String id : userIds) { realm.users().get(id).toRepresentation(); }
    int applied = 0;
    List<String> done = new ArrayList<>();
    try {
      for (String id : userIds) {
        realm.users().get(id).roles().realmLevel().add(List.of(role));
        done.add(id);
        applied++;
      }
      return applied;
    } catch (RuntimeException ex) {
// rollback
      for (String id : done) {
        try { realm.users().get(id).roles().realmLevel().remove(List.of(role)); } catch (Exception ignore) {}
      }
      throw ex;
    }
  }

  public int bulkUnassign(String roleName, List<String> userIds) {
    RoleRepresentation role = roleService.get(roleName);
    for (String id : userIds) { realm.users().get(id).toRepresentation(); }
    int applied = 0;
    for (String id : userIds) {
      realm.users().get(id).roles().realmLevel().remove(List.of(role));
      ensureFallbackIfNoStandard(id);
      applied++;
    }
    return applied;
  }

  /** Rollenwechsel innerhalb der konfigurierten Standardrollen. */
  public int changeStandardRole(List<String> userIds, String targetRole) {
    List<String> allowed = meta.getStandardNames();
    if (!allowed.contains(targetRole)) {
      throw new BadRequestException("Zielrolle nicht in Standardrollen erlaubt: " + targetRole);
    }
    RoleRepresentation target = roleService.get(targetRole);
    int applied = 0;
    for (String id : userIds) {
      // entferne alle Standardrollen, dann Ziel hinzufügen
      List<RoleRepresentation> current = realm.users().get(id).roles().realmLevel().listAll();
      List<RoleRepresentation> toRemove = current.stream()
          .filter(r -> allowed.contains(r.getName()))
          .toList();
      if (!toRemove.isEmpty()) {
        realm.users().get(id).roles().realmLevel().remove(toRemove);
      }
      realm.users().get(id).roles().realmLevel().add(List.of(target));
      applied++;
    }
    return applied;
  }

  /** Wenn Nutzer keine Standardrolle mehr hat, setze Fallback (z. B. "common"). */
  private void ensureFallbackIfNoStandard(String userId) {
    List<String> allowed = meta.getStandardNames();
    String fallback = meta.getFallback();
    List<RoleRepresentation> roles = realm.users().get(userId).roles().realmLevel().listAll();
    boolean hasStd = roles.stream().anyMatch(r -> allowed.contains(r.getName()));
    if (!hasStd) {
      RoleRepresentation fb = roleService.get(fallback);
      realm.users().get(userId).roles().realmLevel().add(List.of(fb));
    }
  }
}
