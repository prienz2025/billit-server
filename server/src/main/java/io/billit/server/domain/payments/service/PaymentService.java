package io.billit.server.domain.payments.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.billit.server.domain.payments.controller.request.PaymentCalculateRequest;
import io.billit.server.domain.payments.controller.request.PaymentConfirmRequest;
import io.billit.server.domain.payments.controller.response.PaymentCalculateResponse;
import io.billit.server.domain.payments.controller.response.RentalHistoryTokenResponse;
import io.billit.server.domain.payments.entity.RentalPayments;
import io.billit.server.domain.payments.repository.RentalPaymentRepository;
import io.billit.server.domain.rentals.entity.RentalHistory;
import io.billit.server.domain.rentals.entity.RentalItems;
import io.billit.server.domain.rentals.entity.RentalStatus;
import io.billit.server.domain.rentals.repository.RentalItemRepository;
import io.billit.server.domain.rentals.service.RentalHistoryService;
import io.billit.server.domain.rentals.service.StockLockService;
import io.billit.server.domain.users.entity.Users;
import io.billit.server.domain.users.repository.UserRepository;
import io.billit.server.global.api.TossPaymentApiClient;
import io.billit.server.global.api.TossPaymentConfirmResponse;
import io.billit.server.global.exceptions.BannabeServiceException;
import io.billit.server.global.exceptions.ErrorCode;
import io.billit.server.global.type.OrderInfo;

@Service
@RequiredArgsConstructor
public class PaymentService {

  private final RentalItemRepository rentalItemRepository;
  private final RentalPaymentRepository rentalPaymentRepository;
  private final UserRepository userRepository;
  private final TossPaymentApiClient tossApiClient;
  private final RentalHistoryService rentalHistoryService;
  private final OrderInfoService orderInfoService;
  private final StockLockService stockLockService;

  @Transactional(readOnly = true)
  public PaymentCalculateResponse calculateAmount(PaymentCalculateRequest paymentRequest) {
    String rentalItemToken = paymentRequest.rentalItemToken();
    Integer rentalItemPrice = rentalItemRepository.findRentalItemPrice(rentalItemToken);
    Integer amount = rentalItemPrice * paymentRequest.rentalTime();
    return new PaymentCalculateResponse(rentalItemToken, rentalItemPrice, paymentRequest.rentalTime(), amount);
  }

  @Transactional
  public RentalHistoryTokenResponse confirmPayment(String entityToken, PaymentConfirmRequest paymentConfirmRequest) {
    OrderInfo orderInfo = getOrderInfoAndValidateAmount(paymentConfirmRequest);
    TossPaymentConfirmResponse paymentConfirmResponse = tossApiClient.confirmPaymentRequest(paymentConfirmRequest);
    RentalHistory rentalHistory = switch (orderInfo.getPaymentType()) {
      case RENT -> handleRentalPayment(entityToken, orderInfo, paymentConfirmResponse);
      case EXTENSION -> handleExtensionPayment(orderInfo, paymentConfirmResponse);
      case OVERDUE -> handleOverduePayment(orderInfo, paymentConfirmResponse);
    };
    orderInfoService.removeOrderInfo(paymentConfirmResponse.orderId());
    return new RentalHistoryTokenResponse(rentalHistory.getToken());
  }

  private OrderInfo getOrderInfoAndValidateAmount(PaymentConfirmRequest paymentConfirmRequest) {
    OrderInfo orderInfo = orderInfoService.findOrderInfoBy(paymentConfirmRequest.orderId());
    if (!orderInfo.getAmount().equals(paymentConfirmRequest.amount())) {
      throw new BannabeServiceException(ErrorCode.AMOUNT_MISMATCH);
    }
    return orderInfo;
  }

  private RentalHistory handleRentalPayment(String entityToken, OrderInfo orderInfo,
      TossPaymentConfirmResponse paymentConfirmResponse) {
    RentalItems rentalItem = rentalItemRepository.findByToken(orderInfo.getRentalItemToken());
    Users user = userRepository.findByToken(entityToken);
    RentalHistory rentalHistory = rentalHistoryService.saveRentalHistory(rentalItem, user, orderInfo,
        paymentConfirmResponse.approvedAt());
    RentalPayments rentalPayments = RentalPayments.create(paymentConfirmResponse, orderInfo, rentalHistory);
    rentalPaymentRepository.save(rentalPayments);
    stockLockService.decreaseStock(rentalItem);
    rentalItem.rentOut();
    return rentalHistory;
  }

  private RentalHistory handleExtensionPayment(OrderInfo orderInfo, TossPaymentConfirmResponse paymentConfirmResponse) {
    String rentalItemToken = orderInfo.getRentalItemToken();
    RentalHistory rentalHistory = rentalHistoryService.findRentalHistoryByRentalItemToken(rentalItemToken);
    rentalHistory.changeStatus(RentalStatus.EXTENSION);
    rentalHistory.extendRentalTime(orderInfo.getRentalTime());
    RentalPayments rentalPayments = RentalPayments.create(paymentConfirmResponse, orderInfo, rentalHistory);
    rentalPaymentRepository.save(rentalPayments);
    return rentalHistory;
  }

  private RentalHistory handleOverduePayment(OrderInfo orderInfo, TossPaymentConfirmResponse paymentConfirmResponse) {
    String rentalItemToken = orderInfo.getRentalItemToken();
    RentalHistory rentalHistory = rentalHistoryService.findRentalHistoryByRentalItemToken(rentalItemToken);
    rentalHistory.changeStatus(RentalStatus.OVERDUE_PAID);
    RentalPayments rentalPayments = RentalPayments.create(paymentConfirmResponse, orderInfo, rentalHistory);
    rentalPaymentRepository.save(rentalPayments);
    return rentalHistory;
  }

}