package site.bannabe.server.domain.payments.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.bannabe.server.domain.payments.controller.request.PaymentCalculateRequest;
import site.bannabe.server.domain.payments.controller.request.PaymentConfirmRequest;
import site.bannabe.server.domain.payments.controller.response.PaymentCalculateResponse;
import site.bannabe.server.domain.payments.controller.response.RentalHistoryTokenResponse;
import site.bannabe.server.domain.payments.entity.PaymentMethod;
import site.bannabe.server.domain.payments.entity.PaymentType;
import site.bannabe.server.domain.payments.entity.RentalPayments;
import site.bannabe.server.domain.payments.repository.RentalPaymentRepository;
import site.bannabe.server.domain.rentals.entity.RentalHistory;
import site.bannabe.server.domain.rentals.entity.RentalItems;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.rentals.entity.RentalStatus;
import site.bannabe.server.domain.rentals.repository.RentalItemRepository;
import site.bannabe.server.domain.rentals.service.RentalHistoryService;
import site.bannabe.server.domain.rentals.service.StockLockService;
import site.bannabe.server.domain.users.entity.Users;
import site.bannabe.server.domain.users.repository.UserRepository;
import site.bannabe.server.global.api.TossPaymentApiClient;
import site.bannabe.server.global.api.TossPaymentConfirmResponse;
import site.bannabe.server.global.api.TossPaymentConfirmResponse.ReceiptInfo;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;
import site.bannabe.server.global.type.OrderInfo;
import site.bannabe.server.global.utils.RandomCodeGenerator;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  private final String rentalItemToken = "rentalItemToken";
  private final String entityToken = "entityToken";
  private final Integer rentalTime = 1;
  private final Integer amount = 1000;
  private final String orderId = "orderId";
  private final String paymentKey = "paymentKey";
  private final PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest(paymentKey, amount, orderId);

  @InjectMocks
  private PaymentService paymentService;
  @Mock
  private RentalItemRepository rentalItemRepository;
  @Mock
  private RentalPaymentRepository rentalPaymentRepository;
  @Mock
  private RentalHistoryService rentalHistoryService;
  @Mock
  private UserRepository userRepository;
  @Mock
  private TossPaymentApiClient tossApiClient;
  @Mock
  private OrderInfoService orderInfoService;
  @Mock
  private StockLockService stockLockService;

  @Test
  @DisplayName("최종 결제 금액 계산 후 데이터 응답")
  void calculateAmount() {
    //given
    Integer rentalItemPrice = 1000;
    PaymentCalculateRequest request = new PaymentCalculateRequest(rentalItemToken, rentalTime);
    given(rentalItemRepository.findRentalItemPrice(rentalItemToken)).willReturn(rentalItemPrice);

    //when
    PaymentCalculateResponse response = paymentService.calculateAmount(request);

    //then
    assertThat(response).isNotNull()
                        .extracting("rentalItemToken", "pricePerHour", "rentalTime", "amount")
                        .containsExactly(rentalItemToken, rentalItemPrice, rentalTime, rentalItemPrice * rentalTime);
  }

  @Test
  @DisplayName("결제 승인 후 대여내역 생성 및 결제정보 저장, 대여내역 토큰 응답, 모든 메서드 정상 호출")
  void confirmPayment() {
    //given
    PaymentType paymentType = PaymentType.RENT;
    OrderInfo orderInfo = new OrderInfo(rentalItemToken, rentalTime, amount, paymentType);
    TossPaymentConfirmResponse confirmResponse = new TossPaymentConfirmResponse(paymentKey, PaymentMethod.CARD, orderId,
        "충전기/1시간", amount, LocalDateTime.now(), new ReceiptInfo("receiptUrl"));
    RentalItems mockItem = mock(RentalItems.class);
    RentalStations mockStation = mock(RentalStations.class);
    Users mockUser = mock(Users.class);
    RentalHistory mockHistory = mock(RentalHistory.class);

    given(orderInfoService.findOrderInfoBy(confirmRequest.orderId())).willReturn(orderInfo);
    given(tossApiClient.confirmPaymentRequest(confirmRequest)).willReturn(confirmResponse);
    given(rentalItemRepository.findByToken(orderInfo.getRentalItemToken())).willReturn(mockItem);
    given(userRepository.findByToken(entityToken)).willReturn(mockUser);
    given(rentalHistoryService.saveRentalHistory(mockItem, mockUser, orderInfo, confirmResponse.approvedAt()))
        .willReturn(mockHistory);
    given(mockHistory.getToken()).willReturn(RandomCodeGenerator.generateRandomToken(RentalHistory.class));

    //when
    RentalHistoryTokenResponse response = paymentService.confirmPayment(entityToken, confirmRequest);

    //then
    assertThat(response).isNotNull()
                        .extracting(RentalHistoryTokenResponse::rentalHistoryToken).asString()
                        .startsWith("RH_")
                        .hasSize(13);

    verify(orderInfoService).findOrderInfoBy(confirmRequest.orderId());
    verify(tossApiClient).confirmPaymentRequest(confirmRequest);
    verify(rentalItemRepository).findByToken(orderInfo.getRentalItemToken());
    verify(userRepository).findByToken(entityToken);
    verify(rentalHistoryService).saveRentalHistory(mockItem, mockUser, orderInfo, confirmResponse.approvedAt());
    verify(rentalPaymentRepository).save(any(RentalPayments.class));
    verify(stockLockService).decreaseStock(mockItem);
    verify(mockItem).rentOut();
    verify(orderInfoService).removeOrderInfo(confirmRequest.orderId());
  }

  @Test
  @DisplayName("주문정보 / 결제요청 간 금액 불일치 시 예외 발생")
  void mismatchAmount() {
    //given
    PaymentType paymentType = PaymentType.RENT;
    OrderInfo orderInfo = new OrderInfo(rentalItemToken, rentalTime, 2000, paymentType);
    given(orderInfoService.findOrderInfoBy(confirmRequest.orderId())).willReturn(orderInfo);

    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> paymentService.confirmPayment(entityToken, confirmRequest))
        .withMessage(ErrorCode.AMOUNT_MISMATCH.getMessage());

    verify(tossApiClient, never()).confirmPaymentRequest(confirmRequest);
    verify(rentalItemRepository, never()).findByToken(rentalItemToken);
    verify(userRepository, never()).findByToken(entityToken);
    verify(rentalPaymentRepository, never()).save(any(RentalPayments.class));
    verify(stockLockService, never()).decreaseStock(any(RentalItems.class));
    verify(orderInfoService, never()).removeOrderInfo(confirmRequest.orderId());
  }

  @Test
  @DisplayName("연장 결제시 대여내역을 수정한다.")
  void confirmExtensionPayment() {
    //given
    PaymentType paymentType = PaymentType.EXTENSION;
    OrderInfo orderInfo = new OrderInfo(rentalItemToken, rentalTime, amount, paymentType);
    TossPaymentConfirmResponse confirmResponse = new TossPaymentConfirmResponse(paymentKey, PaymentMethod.CARD, orderId,
        "충전기/1시간", amount, LocalDateTime.now(), new ReceiptInfo("receiptUrl"));
    RentalHistory rentalHistory = RentalHistory.builder()
                                               .rentalTimeHour(1)
                                               .expectedReturnTime(LocalDateTime.now())
                                               .status(RentalStatus.RENTAL)
                                               .build();
    given(orderInfoService.findOrderInfoBy(confirmRequest.orderId())).willReturn(orderInfo);
    given(tossApiClient.confirmPaymentRequest(confirmRequest)).willReturn(confirmResponse);
    given(rentalHistoryService.findRentalHistoryByRentalItemToken(rentalItemToken)).willReturn(rentalHistory);

    //when
    paymentService.confirmPayment(entityToken, confirmRequest);

    //then
    assertThat(rentalHistory.getStatus()).isEqualTo(RentalStatus.EXTENSION);
    verify(orderInfoService).findOrderInfoBy(confirmRequest.orderId());
    verify(tossApiClient).confirmPaymentRequest(confirmRequest);
    verify(rentalHistoryService).findRentalHistoryByRentalItemToken(rentalItemToken);
  }

  @Test
  @DisplayName("연체 결제시 대여내역을 수정한다.")
  void confirmOverduePayment() {
    //given
    PaymentType paymentType = PaymentType.OVERDUE;
    OrderInfo orderInfo = new OrderInfo(rentalItemToken, rentalTime, amount, paymentType);
    TossPaymentConfirmResponse confirmResponse = new TossPaymentConfirmResponse(paymentKey, PaymentMethod.CARD, orderId,
        "충전기/1시간", amount, LocalDateTime.now(), new ReceiptInfo("receiptUrl"));
    RentalHistory rentalHistory = RentalHistory.builder()
                                               .rentalTimeHour(1)
                                               .expectedReturnTime(LocalDateTime.now())
                                               .status(RentalStatus.OVERDUE)
                                               .build();
    given(orderInfoService.findOrderInfoBy(confirmRequest.orderId())).willReturn(orderInfo);
    given(tossApiClient.confirmPaymentRequest(confirmRequest)).willReturn(confirmResponse);
    given(rentalHistoryService.findRentalHistoryByRentalItemToken(rentalItemToken)).willReturn(rentalHistory);

    //when
    paymentService.confirmPayment(entityToken, confirmRequest);

    //then
    assertThat(rentalHistory.getStatus()).isEqualTo(RentalStatus.OVERDUE_PAID);
    verify(orderInfoService).findOrderInfoBy(confirmRequest.orderId());
    verify(tossApiClient).confirmPaymentRequest(confirmRequest);
    verify(rentalHistoryService).findRentalHistoryByRentalItemToken(rentalItemToken);
  }

}