package com.ase.userservice.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

public class JwtRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
  @Override
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
    if (realmAccess == null) return List.of();
    Object roles = realmAccess.get("roles");
    if (!(roles instanceof Collection<?> roleList)) return List.of();
    return roleList.stream()
        .filter(Objects::nonNull)
        .map(String::valueOf)
        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
        .collect(Collectors.toSet());
  }
}
