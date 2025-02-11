package site.bannabe.server.domain.rentals.entity;

import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.bannabe.server.global.converter.AbstractEnumConverter;

@Getter
@RequiredArgsConstructor
public enum RentalStatus {

  RENTAL("대여중"),
  EXTENSION("연장"),
  OVERDUE("연체"),
  OVERDUE_PAID("연체 납부"),
  RETURNED("반납");

  private final String description;

  @Converter(autoApply = true)
  static class EnumConverter extends AbstractEnumConverter<RentalStatus> {

    public EnumConverter() {
      super(RentalStatus.class);
    }

  }

}