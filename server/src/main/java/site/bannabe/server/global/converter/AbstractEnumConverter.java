package site.bannabe.server.global.converter;

import jakarta.persistence.AttributeConverter;
import java.util.Objects;

public abstract class AbstractEnumConverter<T extends Enum<T>> implements AttributeConverter<T, String> {

  private final Class<T> enumType;

  public AbstractEnumConverter(Class<T> enumType) {
    this.enumType = enumType;
  }

  @Override
  public String convertToDatabaseColumn(T attribute) {
    return attribute.name();
  }

  @Override
  public T convertToEntityAttribute(String dbData) {
    if (Objects.isNull(dbData) || dbData.isEmpty()) {
      return null;
    }

    return Enum.valueOf(enumType, dbData);
  }

}