package com.ase.userservice.services;

import com.ase.userservice.controllers.dto.RoleRequest;
import com.ase.userservice.controllers.dto.RoleUpdateRequest;
import com.ase.userservice.components.exceptions.ConflictException;
import com.ase.userservice.components.exceptions.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.WebApplicationException;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

  private final RealmResource realm;

  public RoleService(RealmResource realm) {
    this.realm = realm;
  }

  public void create(RoleRequest req) {
    RoleRepresentation rep = new RoleRepresentation();
    rep.setName(req.name());
    rep.setDescription(req.description());
    rep.setComposite(false);

    try {
      realm.roles().create(rep); // returns void in KC 26+
    } catch (WebApplicationException ex) {
      Response r = ex.getResponse();
      if (r != null && r.getStatus() == 409) {
        throw new ConflictException("Rolle existiert bereits: " + req.name());
      }
      String msg = (r != null) ? ("Keycloak-Fehler: " + r.getStatus()) : ex.getMessage();
      throw new RuntimeException(msg, ex);
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

  private RoleResource getRoleResourceOrThrow(String name) {
    try {
      return realm.roles().get(name);
    } catch (Exception e) {
      throw new NotFoundException("Rolle nicht gefunden: " + name);
    }
  }
}
