package com.ase.userservice.controllers;

import com.ase.userservice.controllers.dto.RoleRequest;
import com.ase.userservice.controllers.dto.RoleUpdateRequest;
import com.ase.userservice.services.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

  private final RoleService roleService;

  public RoleController(RoleService roleService) {
    this.roleService = roleService;
  }

  @PostMapping
  public ResponseEntity<Void> create(@Valid @RequestBody RoleRequest req, UriComponentsBuilder uri) {
    roleService.create(req);
    return ResponseEntity.created(
        uri.path("/api/roles/{name}").buildAndExpand(req.name()).toUri()
    ).build();
  }

  @PutMapping("/{name}")
  public ResponseEntity<Void> update(@PathVariable String name, @RequestBody RoleUpdateRequest req) {
    roleService.update(name, req);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{name}")
  public ResponseEntity<Void> delete(@PathVariable String name) {
    roleService.delete(name);
    return ResponseEntity.noContent().build();
  }
}
