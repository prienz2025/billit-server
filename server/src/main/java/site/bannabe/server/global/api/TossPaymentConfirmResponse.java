package site.bannabe.server.global.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import site.bannabe.server.domain.payments.entity.PaymentMethod;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentConfirmResponse(
    String paymentKey,

    PaymentMethod method,

    String orderId,

    String orderName,

    Integer totalAmount,

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    LocalDateTime approvedAt,

    ReceiptInfo receipt
) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ReceiptInfo(
      String url
  ) {

  }
}