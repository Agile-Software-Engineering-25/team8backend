package com.ase.userservice.services;

import com.ase.userservice.config.RoleMetadataProperties;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleMetadataService {
  private final RoleMetadataProperties props;
  public RoleMetadataService(RoleMetadataProperties props) { this.props = props; }

  public List<String> getRequiredFields(String roleName) {
    var info = props.getMetadata() != null ? props.getMetadata().get(roleName) : null;
    return info == null ? List.of() : info.getRequiredFields();
  }
}
