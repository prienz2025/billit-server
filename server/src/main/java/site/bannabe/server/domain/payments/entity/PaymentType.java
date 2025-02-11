package site.bannabe.server.domain.payments.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.bannabe.server.global.converter.AbstractEnumConverter;

@Getter
@RequiredArgsConstructor
public enum PaymentType {

  RENT("대여 결제"),
  OVERDUE("연체 결제"),
  EXTENSION("연장 결제");

  private static final Map<String, PaymentType> MAP = Arrays.stream(values()).collect(
      Collectors.toMap(PaymentType::name, Function.identity()));

  private final String description;

  @JsonCreator
  public static PaymentType convert(String data) {
    data = data.toUpperCase();
    return MAP.getOrDefault(data, RENT);
  }

  @Converter(autoApply = true)
  static class EnumConverter extends AbstractEnumConverter<PaymentType> {

    public EnumConverter() {
      super(PaymentType.class);
    }

  }

}