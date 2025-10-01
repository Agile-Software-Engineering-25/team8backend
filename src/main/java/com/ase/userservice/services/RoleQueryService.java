package com.ase.userservice.services;

import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoleQueryService {

  private final RealmResource realm;
  private final RoleService roleService;

  public RoleQueryService(RealmResource realm, RoleService roleService) {
    this.realm = realm;
    this.roleService = roleService;
  }

  public List<RoleRepresentation> getUserDirectRoles(String userId) {
    return realm.users().get(userId).roles().realmLevel().listAll();
  }

  public Set<RoleRepresentation> getUserEffectiveRoles(String userId) {
    List<RoleRepresentation> direct = getUserDirectRoles(userId);
    Set<String> seen = new HashSet<>();
    Set<RoleRepresentation> all = new LinkedHashSet<>();
    for (RoleRepresentation r : direct) {
      expand(r, seen, all);
    }
    return all;
  }

  private void expand(RoleRepresentation role, Set<String> seen, Set<RoleRepresentation> out) {
    if (seen.contains(role.getName())) return;
    seen.add(role.getName());
    out.add(role);
    try {
      var composites = roleService.getComposites(role.getName());
      for (RoleRepresentation c : composites) {
        expand(c, seen, out);
      }
    } catch (Exception ignore) {
    }
  }
}
