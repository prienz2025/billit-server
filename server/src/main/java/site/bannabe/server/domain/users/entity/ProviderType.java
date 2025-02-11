package site.bannabe.server.domain.users.entity;

import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.bannabe.server.global.converter.AbstractEnumConverter;

@Getter
@RequiredArgsConstructor
public enum ProviderType {

  GOOGLE("구글"),
  KAKAO("카카오"),
  LOCAL("로컬");

  private static final Map<String, ProviderType> providerTypeMap =
      Arrays.stream(ProviderType.values()).collect(Collectors.toMap(ProviderType::getDescription, Function.identity()));
  private final String description;

  public static ProviderType getProviderType(String description) {
    return providerTypeMap.get(description);
  }

  @Converter(autoApply = true)
  static class EnumConverter extends AbstractEnumConverter<ProviderType> {

    public EnumConverter() {
      super(ProviderType.class);
    }

  }

}