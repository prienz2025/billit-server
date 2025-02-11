package site.bannabe.server.domain.rentals.controller.response;

import java.util.List;

public record RentalStationDetailResponse(
    List<RentalItemResponse> rentalItems
) {

  public record RentalItemResponse(
      Long itemTypeId,
      String name,
      String image,
      String category,
      Integer stock
  ) {

  }

}