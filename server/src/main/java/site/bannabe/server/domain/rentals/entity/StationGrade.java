package site.bannabe.server.domain.rentals.entity;

import jakarta.persistence.Converter;
import site.bannabe.server.global.converter.AbstractEnumConverter;

public enum StationGrade {

  // Grade 구분을 어떻게 할지 논의가 필요함.
  FIRST, SECOND, THIRD;

  @Converter(autoApply = true)
  static class EnumConverter extends AbstractEnumConverter<StationGrade> {

    public EnumConverter() {
      super(StationGrade.class);
    }

  }

}