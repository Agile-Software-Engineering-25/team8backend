package com.ase.userservice.services;

import com.ase.userservice.controllers.dto.RoleSummary;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleQueryService {

  private final RealmResource realm;

  public RoleQueryService(RealmResource realm) {
    this.realm = realm;
  }

  /**
   * Rollen (realm-level) einer Gruppe
   */
  public List<RoleRepresentation> getGroupRealmRoles(String groupId) {
    GroupResource gr = realm.groups().group(groupId);
    return gr.roles().realmLevel().listAll();
  }

  /**
   * Direkt dem User zugewiesene Rollen (realm-level)
   */
  public List<RoleRepresentation> getUserDirectRealmRoles(String userId) {
    return realm.users().get(userId).roles().realmLevel().listAll();
  }

  /**
   * Gruppenmitgliedschaften des Users
   */
  public List<GroupRepresentation> getUserGroups(String userId) {
    return realm.users().get(userId).groups();
  }

  /**
   * Effektive Rollen eines Users:
   * - direkt zugewiesene
   * - über Gruppen geerbte
   * - inkl. Composite-Erweiterung (rekursiv)
   * -> als RoleSummary inkl. sources
   */
  public List<RoleSummary> getUserEffectiveRoleSummaries(String userId) {
    // name -> Quellen (direct / group:<id>|<name>)
    Map<String, Set<String>> sources = new LinkedHashMap<>();

    // direkte Rollen
    for (RoleRepresentation r : getUserDirectRealmRoles(userId)) {
      sources.computeIfAbsent(r.getName(), k -> new LinkedHashSet<>()).add("direct");
    }

    // Gruppenrollen
    List<GroupRepresentation> groups = getUserGroups(userId);
    for (GroupRepresentation g : groups) {
      for (RoleRepresentation r : getGroupRealmRoles(g.getId())) {
        sources.computeIfAbsent(r.getName(), k -> new LinkedHashSet<>())
            .add("group:" + g.getId() + "|" + g.getName());
      }
    }

    // Composite-Erweiterung (rekursiv um alle abgeleiteten Rollen ergänzen)
    expandCompositesFromNames(sources);

    // RoleRepresentation zu jeder Rolle sichern (für Beschreibung/composite-Flag)
    Map<String, RoleRepresentation> repr = new LinkedHashMap<>();
    for (String name : sources.keySet()) {
      try {
        repr.put(name, realm.roles().get(name).toRepresentation());
      } catch (Exception ignore) {
        // falls Rolle zwischenzeitlich entfernt wurde – überspringen
      }
    }

    // in Summaries umwandeln
    return sources.entrySet().stream()
        .map(e -> {
          RoleRepresentation rr = repr.get(e.getKey());
          String desc = rr != null ? rr.getDescription() : null;
          boolean composite = rr != null && rr.isComposite();
          return new RoleSummary(e.getKey(), desc, composite, new ArrayList<>(e.getValue()));
        })
        .collect(Collectors.toList());
  }

  /* -------------------- Hilfsfunktionen -------------------- */

  /**
   * erweitert die Map (roleName -> sources) um alle Composite-Abhängigkeiten
   */
  private void expandCompositesFromNames(Map<String, Set<String>> sources) {
    boolean added;
    do {
      added = false;
      List<String> snapshot = new ArrayList<>(sources.keySet());
      for (String roleName : snapshot) {
        java.util.Collection<RoleRepresentation> comps = safeGetComposites(roleName);
        for (RoleRepresentation c : comps) {
          // neu hinzugekommene Rollen als "inherited" markieren (Quelle aus übergeordneten Rollen bleibt separat)
          if (!sources.containsKey(c.getName())) {
            sources.put(c.getName(), new LinkedHashSet<>(List.of("inherited")));
            added = true;
          }
        }
      }
    } while (added);
  }

  private java.util.Collection<RoleRepresentation> safeGetComposites(String roleName) {
    try {
      return realm.roles().get(roleName).getRoleComposites();
    } catch (Exception e) {
      return java.util.List.of();
    }
  }
}
