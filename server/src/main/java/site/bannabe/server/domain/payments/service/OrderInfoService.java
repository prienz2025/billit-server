package site.bannabe.server.domain.payments.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.bannabe.server.domain.payments.entity.PaymentType;
import site.bannabe.server.global.redis.OrderInfoClient;
import site.bannabe.server.global.type.OrderInfo;

@Service
@RequiredArgsConstructor
public class OrderInfoService {

  private final OrderInfoClient orderInfoClient;

  public void saveOrderInfo(String orderId, String rentalItemToken, Integer rentalTime, Integer amount, PaymentType paymentType) {
    OrderInfo orderInfo = new OrderInfo(rentalItemToken, rentalTime, amount, paymentType);
    orderInfoClient.save(orderId, orderInfo);
  }

  public OrderInfo findOrderInfoBy(String orderId) {
    return orderInfoClient.findBy(orderId);
  }

  public void removeOrderInfo(String orderId) {
    orderInfoClient.deleteBy(orderId);
  }

  public boolean isExistOrderInfo(String rentalItemToken) {
    return orderInfoClient.existByRentalToken(rentalItemToken);
  }

}