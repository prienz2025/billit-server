package site.bannabe.server.domain.rentals.controller.response;

import site.bannabe.server.domain.rentals.entity.RentalItems;

public record RentalItemDetailResponse(
    String name,
    Integer price,
    String currentStationName
) {

  public static RentalItemDetailResponse create(RentalItems rentalItem) {
    return new RentalItemDetailResponse(
        rentalItem.getRentalItemType().getName(),
        rentalItem.getRentalItemType().getPrice(),
        rentalItem.getCurrentStation().getName()
    );
  }

}