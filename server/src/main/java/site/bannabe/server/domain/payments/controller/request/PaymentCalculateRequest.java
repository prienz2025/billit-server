package site.bannabe.server.domain.payments.controller.request;

public record PaymentCalculateRequest(
    String rentalItemToken,
    Integer rentalTime
) {

}