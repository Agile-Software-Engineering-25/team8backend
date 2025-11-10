package com.ase.userservice.services;

import com.ase.userservice.components.exceptions.NotFoundException;
import com.ase.userservice.controllers.dto.GroupRequest;
import com.ase.userservice.controllers.dto.GroupUpdateRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;

@Service
public class GroupService {
  private final RealmResource realm;

  public GroupService(RealmResource realm) {
    this.realm = realm;
  }
  public RealmResource getRealm() {
    return realm;
  }

  public void create(GroupRequest req) {
    GroupRepresentation g = new GroupRepresentation();
    g.setName(req.name());
    if (req.attributes() != null) g.setAttributes(mapToKC(req.attributes()));
    try {
      realm.groups().add(g); // KC 26+: void
    } catch (WebApplicationException e) { throw e; }
  }

  public List<GroupRepresentation> list() { return realm.groups().groups(); }

  public GroupRepresentation get(String groupId) {
    return group(groupId).toRepresentation();
  }

  public void update(String groupId, GroupUpdateRequest req) {
    GroupResource gr = group(groupId);
    GroupRepresentation g = gr.toRepresentation();
    if (req.name() != null && !req.name().isBlank()) g.setName(req.name());
    if (req.attributes() != null) g.setAttributes(mapToKC(req.attributes()));
    gr.update(g);
  }

  public void delete(String groupId) { group(groupId).remove(); }

  public GroupResource group(String id) {
    try { return realm.groups().group(id); }
    catch (Exception e) { throw new NotFoundException("Gruppe nicht gefunden: " + id); }
  }

  private static Map<String, List<String>> mapToKC(Map<String, String> flat) {
    return flat.entrySet().stream().collect(java.util.stream.Collectors.toMap(
        Map.Entry::getKey, e -> List.of(e.getValue())
    ));
  }
}
