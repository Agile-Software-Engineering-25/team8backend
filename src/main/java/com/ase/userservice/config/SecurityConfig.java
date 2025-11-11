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
            // ALLE API-Endpunkte dieses Backends erlauben
            .requestMatchers("/api/ase-08/**").permitAll()
            // Swagger & Health
            .requestMatchers(
                "/v3/api-docs/**",
                "/ase-08/swagger-ui.html",
                "/ase-08/swagger-ui/**",
                "/api/ase-08/actuator/health/**"
            ).permitAll()
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 ->
            oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConv))
        );
    return http.build();
  }
}
