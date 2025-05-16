package io.billit.server.domain.payments.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.billit.server.domain.users.entity.Users;
import io.billit.server.global.type.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MembershipPayments extends BaseEntity {

  private LocalDateTime paymentDate;

  private LocalDateTime startDate;

  private LocalDateTime expirationDate;

  private Integer extensionCount;

  private Integer totalAmount;

  private String orderId;

  private String paymentKey;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private Users user;

}