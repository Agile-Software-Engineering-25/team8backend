package com.ase.userservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "roles")
public class RoleMetadataProperties {
  private List<String> standardNames = List.of("common");
  private String fallback = "common";
  private Map<String, RoleInfo> metadata;

  public static class RoleInfo {
    private List<String> requiredFields = List.of();
    public List<String> getRequiredFields() { return requiredFields; }
    public void setRequiredFields(List<String> requiredFields) { this.requiredFields = requiredFields; }
  }

  public List<String> getStandardNames() { return standardNames; }
  public void setStandardNames(List<String> standardNames) { this.standardNames = standardNames; }
  public String getFallback() { return fallback; }
  public void setFallback(String fallback) { this.fallback = fallback; }
  public Map<String, RoleInfo> getMetadata() { return metadata; }
  public void setMetadata(Map<String, RoleInfo> metadata) { this.metadata = metadata; }
}
