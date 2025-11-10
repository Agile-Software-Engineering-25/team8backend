package com.ase.userservice.services;

import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.ase.userservice.controllers.dto.RoleNamesRequest;

@Service
public class GroupRoleService {

  private final RealmResource realm;
  private final GroupService groups;

  public GroupRoleService(RealmResource realm, GroupService groups) {
    this.realm = realm;
    this.groups = groups;
  }

  public List<RoleRepresentation> listRealmRoles(String groupId) {
    GroupResource gr = groups.group(groupId);
    return gr.roles().realmLevel().listAll();
  }

  public void addRealmRoles(String groupId, RoleNamesRequest req) {
    GroupResource gr = groups.group(groupId);

    List<RoleRepresentation> reps = req.roleNames().stream()
        .map(n -> realm.roles().get(n).toRepresentation())
        .collect(Collectors.toList());

    // Nur zuordnen, nicht neu anlegen
    gr.roles().realmLevel().add(reps);
  }

  public void removeRealmRoles(String groupId, RoleNamesRequest req) {
    GroupResource gr = groups.group(groupId);

    List<RoleRepresentation> reps = req.roleNames().stream()
        .map(n -> realm.roles().get(n).toRepresentation())
        .collect(Collectors.toList());

    gr.roles().realmLevel().remove(reps);
  }
}
