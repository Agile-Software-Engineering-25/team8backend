package com.ase.userservice.services;

import com.ase.userservice.controllers.dto.RoleNamesRequest;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupRoleService {
  private final RealmResource realm;
  private final GroupService groups;

  public GroupRoleService(RealmResource realm, GroupService groups) {
    this.realm = realm; this.groups = groups;
  }

  public List<RoleRepresentation> listRealmRoles(String groupId) {
    GroupResource gr = groups.group(groupId);
    return gr.roles().realmLevel().listAll();
  }

  public void addRealmRoles(String groupId, RoleNamesRequest req) {
    GroupResource gr = groups.group(groupId);
    List<RoleRepresentation> reps = req.roleNames().stream()
        .map(n -> realm.roles().get(n).toRepresentation())
        .toList();
    gr.roles().realmLevel().add(reps);   // nur zuordnen, nicht erstellen
  }

  public void removeRealmRoles(String groupId, RoleNamesRequest req) {
    GroupResource gr = groups.group(groupId);
    List<RoleRepresentation> reps = req.roleNames().stream()
        .map(n -> realm.roles().get(n).toRepresentation())
        .toList();
    gr.roles().realmLevel().remove(reps);
  }
}
