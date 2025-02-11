package site.bannabe.server.domain.users.entity;

import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import site.bannabe.server.global.converter.AbstractEnumConverter;

@RequiredArgsConstructor
public enum Role {

  ADMIN("ROLE_ADMIN"),
  MANAGER("ROLE_MANAGER"),
  USER("ROLE_USER");

  private final String key;

  public String getRoleKey() {
    return key;
  }

  @Converter(autoApply = true)
  static class EnumConverter extends AbstractEnumConverter<Role> {

    public EnumConverter() {
      super(Role.class);
    }

  }

}