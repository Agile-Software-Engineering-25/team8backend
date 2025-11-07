package com.ase.userservice.services;

import com.ase.userservice.components.exceptions.NotFoundException;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
  private final RealmResource realm;

  public UserService(RealmResource realm) { this.realm = realm; }

  public List<UserRepresentation> search(String query, Integer first, Integer max) {
    int f = first == null ? 0 : first;
    int m = max == null ? 50 : Math.min(max, 200);
    return realm.users().search(query == null ? "" : query, f, m);
  }

  public UserRepresentation getById(String userId) {
    try {
      return realm.users().get(userId).toRepresentation();
    } catch (Exception e) {
      throw new NotFoundException("User nicht gefunden: " + userId);
    }
  }

  public UserResource getResource(String userId) {
    try {
      return realm.users().get(userId);
    } catch (Exception e) {
      throw new NotFoundException("User nicht gefunden: " + userId);
    }
  }
}
