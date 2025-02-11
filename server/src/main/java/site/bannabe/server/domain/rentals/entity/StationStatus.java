package site.bannabe.server.domain.rentals.entity;

import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.bannabe.server.global.converter.AbstractEnumConverter;

@Getter
@RequiredArgsConstructor
public enum StationStatus {

  OPEN("운영 중"),
  CLOSE("운영 종료");

  private final String description;

  @Converter(autoApply = true)
  static class EnumConverter extends AbstractEnumConverter<StationStatus> {

    public EnumConverter() {
      super(StationStatus.class);
    }

  }

}