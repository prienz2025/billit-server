package io.billit.server.domain.rentals.controller.response;

public record RentalSuccessSimpleResponse(
    Integer totalAmount,
    String itemName,
    Integer rentalTime,
    String rentalStationName
) {

}