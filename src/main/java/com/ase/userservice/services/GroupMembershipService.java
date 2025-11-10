package com.ase.userservice.services;

import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.stereotype.Service;

import com.ase.userservice.controllers.dto.BulkUserIdsRequest;

@Service
public class GroupMembershipService {

  private final RealmResource realm;
  private final GroupService groups;

  public GroupMembershipService(RealmResource realm, GroupService groups) {
    this.realm = realm;
    this.groups = groups;
  }

  public void addUser(String userId, String groupId) {
    // wirft Fehler, falls Gruppe nicht existiert
    groups.group(groupId);
    realm.users().get(userId).joinGroup(groupId);
  }

  public void removeUser(String userId, String groupId) {
    groups.group(groupId);
    realm.users().get(userId).leaveGroup(groupId);
  }

  public int addUsersBulk(String groupId, BulkUserIdsRequest body) {
    groups.group(groupId); // Existenz prüfen
    int n = 0;
    for (String id : body.userIds()) {
      realm.users().get(id).joinGroup(groupId);
      n++;
    }
    return n;
  }

  public int removeUsersBulk(String groupId, BulkUserIdsRequest body) {
    groups.group(groupId); // Existenz prüfen
    int n = 0;
    for (String id : body.userIds()) {
      realm.users().get(id).leaveGroup(groupId);
      n++;
    }
    return n;
  }
}
