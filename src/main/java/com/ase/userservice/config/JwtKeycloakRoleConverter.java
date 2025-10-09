package com.ase.userservice.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

public class JwtKeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
  @Override
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    Set<GrantedAuthority> out = new HashSet<>();

    // Realm-Rollen -> ROLE_<role>
    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
    if (realmAccess != null) {
      Object roles = realmAccess.get("roles");
      if (roles instanceof Collection<?> col) {
        col.forEach(r -> out.add(new SimpleGrantedAuthority("ROLE_" + r)));
      }
    }

    // Client-Rollen -> ROLE_<client>_<role>, z. B. ROLE_realm-management_manage-users
    Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
    if (resourceAccess != null) {
      resourceAccess.forEach((client, v) -> {
        if (v instanceof Map<?, ?> m) {
          Object roles = m.get("roles");
          if (roles instanceof Collection<?> col) {
            col.forEach(r -> out.add(new SimpleGrantedAuthority("ROLE_" + client + "_" + r)));
          }
        }
      });
    }
    return out;
  }
}
