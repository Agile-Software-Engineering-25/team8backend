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

import jakarta.ws.rs.core.Response;
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
    return getAllGroupsFlat().stream()
        .filter(g -> !isStandardRootGroup(g))   // Standardgruppen ausblenden
        .map(this::toGroupDto)
        .sorted(Comparator.comparing(GroupDto::name))
        .collect(Collectors.toList());
  }

  public List<GroupDto> findStandardGroups() {
    return getRealm().groups().groups().stream()
        .filter(g -> STANDARD_GROUP_NAMES.contains(g.getName()))
        .map(this::toGroupDto)
        .collect(Collectors.toList());
  }

  public GroupDetailDto findGroupById(String groupId) {
    GroupRepresentation self = realm.groups()
        .group(groupId)
        .toRepresentation();
    return buildGroupDetail(self);
  }

  public GroupDetailDto findGroupByName(String name) {
    GroupRepresentation self = getAllGroupsFlat().stream()
        .filter(g -> name.equals(g.getName()))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Group not found: " + name));
    return buildGroupDetail(self);
  }


  public String createGroup(CreateGroupRequest request) {
    GroupRepresentation g = new GroupRepresentation();
    g.setName(request.name());
    if (request.attributes() != null) {
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

  public GroupDto createChildGroup(String parentGroupId, CreateGroupRequest request) {
    GroupRepresentation rep = new GroupRepresentation();
    rep.setName(request.name());
    if (request.attributes() != null) {
      Map<String, List<String>> attrs = request.attributes().entrySet().stream()
          .collect(Collectors.toMap(
              Map.Entry::getKey,
              e -> List.of(e.getValue())
          ));
      rep.setAttributes(attrs);
    }

    Response response = realm.groups()
        .group(parentGroupId)
        .subGroup(rep);   // Child unter Parent anlegen

    String location = response.getHeaderString("Location");
    String childId = location.substring(location.lastIndexOf('/') + 1);

    GroupRepresentation created = realm.groups()
        .group(childId)
        .toRepresentation();

    return toGroupDto(created);
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

  // alle Gruppen (inkl. Child-Gruppen) flach sammeln
  private List<GroupRepresentation> getAllGroupsFlat() {
    List<GroupRepresentation> result = new ArrayList<>();
    Deque<GroupRepresentation> stack = new ArrayDeque<>(realm.groups().groups());
    while (!stack.isEmpty()) {
      GroupRepresentation g = stack.pop();
      result.add(g);
      if (g.getSubGroups() != null) {
        stack.addAll(g.getSubGroups());
      }
    }
    return result;
  }

  private boolean isStandardRootGroup(GroupRepresentation g) {
    String path = g.getPath(); // z.B. /Student
    int depth = path == null ? 0 : path.split("/").length; // ["", "Student"] => 2
    return depth == 2 && STANDARD_GROUP_NAMES.contains(g.getName());
  }

  private String extractParentName(GroupRepresentation g) {
    String path = g.getPath(); // z.B. /Student/BIN-T25/Gruppe3
    if (path == null) {
      return null;
    }
    String[] segments = path.split("/");
    if (segments.length <= 2) { // nur Root-Gruppe
      return null;
    }
    return segments[segments.length - 2];
  }

  private GroupDto toGroupDto(GroupRepresentation g) {
    long memberCount = realm.groups().group(g.getId())
        .members().size();
    String parentName = extractParentName(g);
    return new GroupDto(g.getId(), g.getName(), memberCount, parentName);
  }

  private GroupDetailDto buildGroupDetail(GroupRepresentation self) {
    List<GroupRepresentation> all = getAllGroupsFlat();

    // direkte Permissions dieser Gruppe
    List<String> permissionIds = realm.groups()
        .group(self.getId())
        .roles().realmLevel().listAll().stream()
        .map(RoleRepresentation::getId)
        .collect(Collectors.toList());

    String parentName = extractParentName(self);

    // Ancestors anhand des Pfades bestimmen
    List<GroupRefDto> ancestors = new ArrayList<>();
    String path = self.getPath(); // /Student/BIN-T25/Gruppe3
    if (path != null) {
      String[] segments = path.split("/");
      String currentPath = "";
      for (int i = 1; i < segments.length - 1; i++) {
        currentPath += "/" + segments[i];
        String p = currentPath;
        all.stream()
            .filter(g -> p.equals(g.getPath()))
            .findFirst()
            .ifPresent(g -> ancestors.add(new GroupRefDto(g.getId(), g.getName())));
      }
    }

    // direkte Children
    List<GroupRefDto> children = new ArrayList<>();
    if (self.getSubGroups() != null) {
      for (GroupRepresentation sg : self.getSubGroups()) {
        children.add(new GroupRefDto(sg.getId(), sg.getName()));
      }
    }

    return new GroupDetailDto(
        self.getId(),
        self.getName(),
        self.getAttributes(),
        permissionIds,
        parentName,
        ancestors,
        children
    );
  }
}
