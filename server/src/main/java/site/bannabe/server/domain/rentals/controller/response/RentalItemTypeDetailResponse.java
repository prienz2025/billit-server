package site.bannabe.server.domain.rentals.controller.response;

public record RentalItemTypeDetailResponse(
    String name,
    String image,
    String category,
    String description,
    Integer price,
    Integer stock
) {

}