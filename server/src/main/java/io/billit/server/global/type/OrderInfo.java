package io.billit.server.global.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.billit.server.domain.payments.entity.PaymentType;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderInfo {

  private String rentalItemToken;

  private Integer rentalTime;

  private Integer amount;

  private PaymentType paymentType;

}