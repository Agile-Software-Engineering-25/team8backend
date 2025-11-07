package com.ase.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    var jwtConv = new JwtAuthenticationConverter();
    jwtConv.setJwtGrantedAuthoritiesConverter(new JwtKeycloakRoleConverter());

    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            // GROUPS lesen
            .requestMatchers(HttpMethod.GET, "/api/groups/**").hasAnyAuthority(
                "ROLE_realm-admin",
                "ROLE_realm-management_view-realm",
                "ROLE_realm-management_manage-realm"
            )
            // GROUPS anlegen/ändern/löschen + Rollen an Gruppe
            .requestMatchers("/api/groups/**").hasAnyAuthority(
                "ROLE_realm-admin",
                "ROLE_realm-management_manage-realm"
            )
            // USERS lesen (für Suche etc.)
            .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyAuthority(
                "ROLE_realm-admin",
                "ROLE_realm-management_view-users",
                "ROLE_realm-management_query-users",
                "ROLE_realm-management_manage-users"
            )
            // Gruppenmitgliedschaften (zuordnen/entziehen)
            .requestMatchers("/api/users/**").hasAnyAuthority(
                "ROLE_realm-admin",
                "ROLE_realm-management_manage-users"
            )
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConv)));

    return http.build();
  }
}
