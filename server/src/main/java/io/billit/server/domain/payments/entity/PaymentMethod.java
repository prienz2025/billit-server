package io.billit.server.domain.payments.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import io.billit.server.global.converter.AbstractEnumConverter;
import io.billit.server.global.exceptions.BannabeServiceException;
import io.billit.server.global.exceptions.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {

  CARD("카드"),
  EASY_PAY("간편결제");

  private final String value;

  @JsonCreator
  public static PaymentMethod convert(String value) {
    for (PaymentMethod paymentMethod : values()) {
      if (paymentMethod.value.equals(value)) {
        return paymentMethod;
      }
    }
    throw new BannabeServiceException(ErrorCode.INTERNAL_SERVER_ERROR); // 예외 수정해야함.
  }

  @Converter(autoApply = true)
  static class EnumConverter extends AbstractEnumConverter<PaymentMethod> {

    public EnumConverter() {
      super(PaymentMethod.class);
    }
  }

}