package site.bannabe.server.domain.payments.controller.response;

public record PaymentCalculateResponse(
    String rentalItemToken,
    Integer pricePerHour,
    Integer rentalTime,
    Integer amount
) {

}