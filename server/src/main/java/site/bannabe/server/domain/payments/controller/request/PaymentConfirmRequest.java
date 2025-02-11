package site.bannabe.server.domain.payments.controller.request;

public record PaymentConfirmRequest(
    String paymentKey,

    Integer amount,

    String orderId
) {

}