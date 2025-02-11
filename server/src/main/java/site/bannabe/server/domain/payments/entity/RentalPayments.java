package site.bannabe.server.domain.payments.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.bannabe.server.domain.rentals.entity.RentalHistory;
import site.bannabe.server.global.api.TossPaymentConfirmResponse;
import site.bannabe.server.global.type.BaseEntity;
import site.bannabe.server.global.type.OrderInfo;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RentalPayments extends BaseEntity {

  private PaymentType paymentType;

  @Default
  private PaymentStatus status = PaymentStatus.APPROVED;

  private PaymentMethod paymentMethod;

  @Column(columnDefinition = "datetime")
  private LocalDateTime paymentDate;

  private String orderName;

  private Integer totalAmount;

  private String orderId;

  private String paymentKey;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rental_history_id")
  private RentalHistory rentalHistory;

  private RentalPayments(PaymentType paymentType, PaymentStatus status, PaymentMethod paymentMethod, LocalDateTime paymentDate,
      String orderName, Integer totalAmount, String orderId, String paymentKey, RentalHistory rentalHistory) {
    this.paymentType = paymentType;
    this.status = status;
    this.paymentMethod = paymentMethod;
    this.paymentDate = paymentDate;
    this.orderName = orderName;
    this.totalAmount = totalAmount;
    this.orderId = orderId;
    this.paymentKey = paymentKey;
    this.rentalHistory = rentalHistory;
  }

  public static RentalPayments create(TossPaymentConfirmResponse paymentResponse, OrderInfo orderInfo,
      RentalHistory rentalHistory) {
    return RentalPayments.builder()
                         .paymentType(orderInfo.getPaymentType())
                         .paymentMethod(paymentResponse.method())
                         .paymentDate(paymentResponse.approvedAt())
                         .orderName(paymentResponse.orderName())
                         .totalAmount(paymentResponse.totalAmount())
                         .orderId(paymentResponse.orderId())
                         .paymentKey(paymentResponse.paymentKey())
                         .rentalHistory(rentalHistory)
                         .build();
  }

}