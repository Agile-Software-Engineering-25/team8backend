package com.ase.userservice.services;

import com.ase.userservice.controllers.dto.*;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class KeycloakGroupService {

  private static final Set<String> STANDARD_GROUP_NAMES = Set.of(
      "Lecturer",
      "SAU Admin",
      "Student",
      "University administrative staff"
  );

  private final RealmResource realm;

  public KeycloakGroupService(RealmResource realm) {
    this.realm = realm;
  }

  public RealmResource getRealm() {
    return realm;
  }

  /* ===================== Groups ===================== */

  public List<GroupDto> findAllGroups() {
    return getRealm().groups().groups().stream()
        .filter(g -> !STANDARD_GROUP_NAMES.contains(g.getName()))
        .map(this::toGroupDto)
        .collect(Collectors.toList());
  }

  public List<GroupDto> findStandardGroups() {
    return getRealm().groups().groups().stream()
        .filter(g -> STANDARD_GROUP_NAMES.contains(g.getName()))
        .map(this::toGroupDto)
        .collect(Collectors.toList());
  }

  public GroupDetailDto findGroupById(String groupId) {
    GroupResource gr = getRealm().groups().group(groupId);
    GroupRepresentation g = gr.toRepresentation();

    List<String> permissionIds = gr.roles().realmLevel().listAll().stream()
        .map(RoleRepresentation::getId)
        .toList();

    return new GroupDetailDto(
        g.getId(),
        g.getName(),
        g.getAttributes(),
        permissionIds
    );
  }

  public List<GroupDto> searchGroupsByName(String name) {
    String query = name == null ? "" : name.toLowerCase(Locale.ROOT);
    return findAllGroups().stream()
        .filter(g -> g.name() != null &&
            g.name().toLowerCase(Locale.ROOT).contains(query))
        .toList();
  }

  public String createGroup(CreateGroupRequest request) {
    GroupRepresentation g = new GroupRepresentation();
    g.setName(request.name());
    if (request.attributes() != null && !request.attributes().isEmpty()) {
      Map<String, List<String>> attrs = request.attributes().entrySet().stream()
          .collect(Collectors.toMap(
              Map.Entry::getKey,
              e -> List.of(e.getValue())
          ));
      g.setAttributes(attrs);
    }
    getRealm().groups().add(g); // KC 26: void
    // ID nicht direkt nötig – das Frontend kann anschließend /groups abfragen
    return g.getName();
  }

  public void updateGroup(String groupId, UpdateGroupRequest request) {
    GroupResource gr = getRealm().groups().group(groupId);
    GroupRepresentation g = gr.toRepresentation();

    if (request.name() != null && !request.name().isBlank()) {
      g.setName(request.name());
    }
    if (request.attributes() != null) {
      Map<String, List<String>> attrs = request.attributes().entrySet().stream()
          .collect(Collectors.toMap(
              Map.Entry::getKey,
              e -> List.of(e.getValue())
          ));
      g.setAttributes(attrs);
    }
    gr.update(g);
  }

  public void deleteGroup(String groupId) {
    getRealm().groups().group(groupId).remove();
  }

  /* ===================== Permissions einer Group ===================== */

  public List<RoleRepresentation> findGroupPermissions(String groupId) {
    return getRealm().groups().group(groupId)
        .roles().realmLevel().listAll();
  }

  public void addPermissionsToGroup(String groupId, GroupPermissionsRequest request) {
    GroupResource gr = getRealm().groups().group(groupId);
    List<RoleRepresentation> roles = request.permissionIds().stream()
        .map(id -> getRealm().rolesById().getRole(id))
        .toList();
    gr.roles().realmLevel().add(roles);
  }

  public void removePermissionsFromGroup(String groupId, GroupPermissionsRequest request) {
    GroupResource gr = getRealm().groups().group(groupId);
    List<RoleRepresentation> roles = request.permissionIds().stream()
        .map(id -> getRealm().rolesById().getRole(id))
        .toList();
    gr.roles().realmLevel().remove(roles);
  }

  /* ===================== Group Membership ===================== */

  public List<UserDto> getGroupMembers(String groupId, int first, int max) {
    List<UserRepresentation> members =
        getRealm().groups().group(groupId).members(first, max);
    return members.stream()
        .map(this::toUserDto)
        .toList();
  }

  public void addUsersToGroup(String groupId, BulkUserIdsRequest body) {
    for (String userId : body.userIds()) {
      getRealm().users().get(userId).joinGroup(groupId);
    }
  }

  public void removeUsersFromGroup(String groupId, BulkUserIdsRequest body) {
    for (String userId : body.userIds()) {
      getRealm().users().get(userId).leaveGroup(groupId);
    }
  }

  public void addSingleUserToGroup(String groupId, String userId) {
    getRealm().users().get(userId).joinGroup(groupId);
  }

  public void removeSingleUserFromGroup(String groupId, String userId) {
    getRealm().users().get(userId).leaveGroup(groupId);
  }

  /* ===================== Users / Rollenübersicht ===================== */

  public List<UserDto> searchUsers(String q, int first, int max) {
    List<UserRepresentation> users =
        getRealm().users().search(q == null ? "" : q, first, max);
    return users.stream().map(this::toUserDto).toList();
  }

  public UserDto getUserById(String userId) {
    return toUserDto(getRealm().users().get(userId).toRepresentation());
  }

  public List<com.ase.userservice.controllers.dto.PermissionDto> getUserEffectivePermissions(String userId) {
    Set<RoleRepresentation> roles = new HashSet<>();

    // direkt zugewiesene Realm-Rollen
    roles.addAll(getRealm().users().get(userId).roles().realmLevel().listAll());

    // Gruppenrollen
    List<GroupRepresentation> groups = getRealm().users().get(userId).groups();
    for (GroupRepresentation g : groups) {
      roles.addAll(getRealm().groups().group(g.getId())
          .roles().realmLevel().listAll());
    }

    return roles.stream()
        .map(r -> new com.ase.userservice.controllers.dto.PermissionDto(
            r.getId(), r.getName()
        ))
        .toList();
  }

  /* ===================== Helper ===================== */

  private UserDto toUserDto(UserRepresentation u) {
    return new UserDto(
        u.getId(),
        u.getUsername(),
        u.getEmail(),
        Boolean.TRUE.equals(u.isEnabled())
    );
  }

  private GroupDto toGroupDto(GroupRepresentation g) {
    long memberCount = getRealm().groups()
        .group(g.getId())
        .members()
        .size();
    return new GroupDto(g.getId(), g.getName(), memberCount);
  }
}
