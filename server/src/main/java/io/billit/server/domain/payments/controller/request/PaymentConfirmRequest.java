package io.billit.server.domain.payments.controller.request;

public record PaymentConfirmRequest(
    String paymentKey,

    Integer amount,

    String orderId
) {

}