package io.billit.server.domain.payments.controller.response;

public record PaymentInitializeResponse(
    String apiKey,
    Integer amount,
    String currency,
    String customerKey,
    String orderId,
    String orderName
) {

}