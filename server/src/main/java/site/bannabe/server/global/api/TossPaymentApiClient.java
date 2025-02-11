package site.bannabe.server.global.api;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import site.bannabe.server.domain.payments.controller.request.PaymentConfirmRequest;

@Component
@RequiredArgsConstructor
public class TossPaymentApiClient {

  // onStatus를 사용해서 예외처리 해야함.
  // https://docs.tosspayments.com/reference/error-codes#%EC%BD%94%EC%96%B4-api-%EB%B3%84-%EC%97%90%EB%9F%AC
  private static final String PAYMENT_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

  private final RestClient restClient;

  @Value("${bannabe.payment.secret-key}")
  private String secretKey;

  public TossPaymentConfirmResponse confirmPaymentRequest(PaymentConfirmRequest paymentConfirmRequest) {
    return restClient.post()
                     .uri(PAYMENT_CONFIRM_URL)
                     .headers(header -> {
                       header.setBasicAuth(secretKey, "");
                       header.setContentType(MediaType.APPLICATION_JSON);
                     })
                     .body(paymentConfirmRequest)
                     .retrieve()
                     .body(TossPaymentConfirmResponse.class);
  }

}