package site.bannabe.server.domain.payments.service;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.bannabe.server.domain.payments.controller.response.PaymentInitializeResponse;
import site.bannabe.server.domain.payments.entity.PaymentType;
import site.bannabe.server.domain.rentals.entity.RentalItemTypes;
import site.bannabe.server.domain.rentals.entity.RentalItems;
import site.bannabe.server.domain.rentals.repository.RentalItemRepository;
import site.bannabe.server.global.aop.DistributedLock;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;
import site.bannabe.server.global.utils.RandomCodeGenerator;

@Service
@RequiredArgsConstructor
public class PaymentViewService {

  private static final String CURRENCY = "KRW";

  private final RentalItemRepository rentalItemRepository;
  private final OrderInfoService orderInfoService;

  @Value("${bannabe.payment.api-key}")
  private String apiKey;

  @DistributedLock(key = "#rentalItemToken")
  public PaymentInitializeResponse getPaymentRequest(String rentalItemToken, Integer rentalTime, PaymentType paymentType) {
    validateOrderInfoNotExist(rentalItemToken);
    RentalItems rentalItem = rentalItemRepository.findByToken(rentalItemToken);
    RentalItemTypes rentalItemType = rentalItem.getRentalItemType();
    String orderName = createOrderName(rentalItemType.getName(), rentalTime);
    String customerKey = UUID.randomUUID().toString();
    String orderId = RandomCodeGenerator.generateOrderId(LocalDateTime.now());
    Integer amount = rentalItemType.getPrice() * rentalTime;
    orderInfoService.saveOrderInfo(orderId, rentalItemToken, rentalTime, amount, paymentType);
    return new PaymentInitializeResponse(apiKey, amount, CURRENCY, customerKey, orderId, orderName);
  }

  private void validateOrderInfoNotExist(String rentalItemToken) {
    if (orderInfoService.isExistOrderInfo(rentalItemToken)) {
      throw new BannabeServiceException(ErrorCode.ORDER_INFO_EXISTS);
    }
  }

  private String createOrderName(String itemName, Integer rentalTime) {
    return itemName + "/" + rentalTime + "시간";
  }

}