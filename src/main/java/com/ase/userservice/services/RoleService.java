package com.ase.userservice.services;

import com.ase.userservice.controllers.dto.RoleRequest;
import com.ase.userservice.controllers.dto.RoleUpdateRequest;
import com.ase.userservice.components.exceptions.ConflictException;
import com.ase.userservice.components.exceptions.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

  private final RealmResource realm;

  public RoleService(RealmResource realm) { this.realm = realm; }

  public void create(RoleRequest req) {
    RoleRepresentation rep = new RoleRepresentation();
    rep.setName(req.name());
    rep.setDescription(req.description());
    rep.setComposite(false);
    try {
      realm.roles().create(rep); // KC 26+: void
    } catch (WebApplicationException ex) {
      var r = ex.getResponse();
      if (r != null && r.getStatus() == 409) {
        throw new ConflictException("Rolle existiert bereits: " + req.name());
      }
      throw ex;
    }
  }

  public void update(String currentName, RoleUpdateRequest req) {
    RoleResource roleResource = getRoleResourceOrThrow(currentName);
    RoleRepresentation rep = roleResource.toRepresentation();
    if (req.description() != null) rep.setDescription(req.description());
    if (req.newName() != null && !req.newName().isBlank()) rep.setName(req.newName());
    roleResource.update(rep);
  }

  public void delete(String name) {
    RoleResource roleResource = getRoleResourceOrThrow(name);
    roleResource.remove();
  }

  public List<RoleRepresentation> listAll() {
    return realm.roles().list();
  }

  public RoleRepresentation get(String name) {
    return getRoleResourceOrThrow(name).toRepresentation();
  }

  public List<RoleRepresentation> getComposites(String name) {
    return (List<RoleRepresentation>) getRoleResourceOrThrow(name).getRoleComposites();
  }

  private RoleResource getRoleResourceOrThrow(String name) {
    try {
      return realm.roles().get(name);
    } catch (Exception e) {
      throw new NotFoundException("Rolle nicht gefunden: " + name);
    }
  }
}
