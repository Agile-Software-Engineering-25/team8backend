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
    List<GroupRepresentation> all = getAllGroupsFlat();

    return all.stream()
        .map(g -> {
          long memberCount = realm.groups()
              .group(g.getId())
              .members()
              .size();

          String parentName = extractParentName(g);

          return new GroupDto(
              g.getId(),
              g.getName(),
              memberCount,
              parentName
          );
        })
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
      // Map<String, String> -> Map<String, List<String>>
      Map<String, List<String>> attrs = request.attributes().entrySet().stream()
          .collect(Collectors.toMap(
              Map.Entry::getKey,
              e -> List.of(e.getValue())
          ));
      rep.setAttributes(attrs);
    }

    RealmResource realm = getRealm();
    GroupResource parent = realm.groups().group(parentGroupId);

    // Kindgruppe in Keycloak anlegen
    Response response = parent.subGroup(rep);
    String location = response.getLocation().getPath();
    String childId = location.substring(location.lastIndexOf('/') + 1);

    GroupResource child = realm.groups().group(childId);

    // ======= Rollen vom Parent erben =======
    List<RoleRepresentation> parentRoles =
        parent.roles().realmLevel().listAll();
    if (!parentRoles.isEmpty()) {
      child.roles().realmLevel().add(parentRoles);
    }
    // =======================================

    GroupRepresentation created = child.toRepresentation();
    long memberCount = realm.groups().group(created.getId())
        .members().size();

    return new GroupDto(
        created.getId(),
        created.getName(),
        memberCount,
        parent.toRepresentation().getName()
    );
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

    // Top-Level-Groups holen
    List<GroupRepresentation> roots = realm.groups().groups();
    for (GroupRepresentation root : roots) {
      collectWithChildren(root, result);
    }
    return result;
  }

  private void collectWithChildren(GroupRepresentation current, List<GroupRepresentation> acc) {
    acc.add(current);

    // vollständige Repräsentation inkl. Subgroups laden
    GroupRepresentation full = realm.groups()
        .group(current.getId())
        .toRepresentation();

    List<GroupRepresentation> children = full.getSubGroups();
    if (children == null) {
      return;
    }

    for (GroupRepresentation child : children) {
      collectWithChildren(child, acc);
    }
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

  private void collectGroupDtos(RealmResource realm,
                                GroupRepresentation group,
                                String parentName,
                                List<GroupDto> target) {

    long memberCount = realm.groups()
        .group(group.getId())
        .members().size();

    target.add(new GroupDto(group.getId(), group.getName(), memberCount, parentName));

    if (group.getSubGroups() != null) {
      for (GroupRepresentation child : group.getSubGroups()) {
        collectGroupDtos(realm, child, group.getName(), target);
      }
    }
  }

  private GroupDetailDto buildGroupDetail(GroupRepresentation self) {
    // alle Gruppen flach – wird für die Ancestors benötigt
    List<GroupRepresentation> all = getAllGroupsFlat();

    // ===== 1) direkte Permissions dieser Gruppe =====
    List<PermissionDto> permissions = realm.groups()
        .group(self.getId())
        .roles()
        .realmLevel()
        .listAll()
        .stream()
        .map(r -> new PermissionDto(r.getId(), r.getName()))
        .collect(Collectors.toList());

    // ===== 2) Parent-Name aus dem Pfad bestimmen =====
    String parentName = extractParentName(self);

    // ===== 3) Ancestors anhand des Pfades bestimmen =====
    List<GroupRefDto> ancestors = new ArrayList<>();
    String path = self.getPath();         // z.B. "/Student/BIN-T25/Gruppe3"
    if (path != null) {
      String[] segments = path.split("/");
      String currentPath = "";
      // alle Segmente außer der letzten (das ist die aktuelle Gruppe)
      for (int i = 1; i < segments.length - 1; i++) {
        currentPath += "/" + segments[i];
        String p = currentPath;

        all.stream()
            .filter(g -> p.equals(g.getPath()))
            .findFirst()
            .ifPresent(g ->
                ancestors.add(new GroupRefDto(g.getId(), g.getName()))
            );
      }
    }

    // ===== 4) direkte Children =====
    List<GroupRefDto> children = new ArrayList<>();

    GroupRepresentation full = realm.groups()
        .group(self.getId())
        .toRepresentation();

    List<GroupRepresentation> directChildren = full.getSubGroups();
    if (directChildren != null) {
      for (GroupRepresentation child : directChildren) {
        children.add(new GroupRefDto(child.getId(), child.getName()));
      }
    }

    // ===== 5) GroupDetailDto zusammenbauen =====
    return new GroupDetailDto(
        self.getId(),
        self.getName(),
        self.getAttributes(),
        permissions,   // jetzt List<PermissionDto>
        parentName,
        ancestors,
        children
    );
  }
}
