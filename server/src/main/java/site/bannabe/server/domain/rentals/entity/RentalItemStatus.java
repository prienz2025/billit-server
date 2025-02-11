package site.bannabe.server.domain.rentals.entity;

import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.bannabe.server.global.converter.AbstractEnumConverter;

@Getter
@RequiredArgsConstructor
public enum RentalItemStatus {

  AVAILABLE("대여 가능"),
  RENTED("대여 중"),
  CRASHED("고장"),
  LOST("분실");

  private final String description;

  @Converter(autoApply = true)
  static class EnumConverter extends AbstractEnumConverter<RentalItemStatus> {

    public EnumConverter() {
      super(RentalItemStatus.class);
    }
  }

}