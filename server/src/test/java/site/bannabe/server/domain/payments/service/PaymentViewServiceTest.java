package site.bannabe.server.domain.payments.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import site.bannabe.server.domain.payments.controller.response.PaymentInitializeResponse;
import site.bannabe.server.domain.payments.entity.PaymentType;
import site.bannabe.server.domain.rentals.entity.RentalItemTypes;
import site.bannabe.server.domain.rentals.entity.RentalItems;
import site.bannabe.server.domain.rentals.repository.RentalItemRepository;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;

@ExtendWith(MockitoExtension.class)
class PaymentViewServiceTest {

  @InjectMocks
  private PaymentViewService paymentViewService;
  @Mock
  private RentalItemRepository rentalItemRepository;
  @Mock
  private OrderInfoService orderInfoService;

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(paymentViewService, "apiKey", "test");
  }

  @Test
  @DisplayName("결제 요청 데이터 생성 성공")
  void getPaymentRequest() {
    //given
    String rentalItemToken = "rentalItemToken";
    Integer rentalTime = 1;
    PaymentType paymentType = PaymentType.RENT;
    String rentalItemName = "rentalItemName";
    Integer rentalItemPrice = 1000;
    String orderName = rentalItemName + "/" + rentalTime + "시간";
    RentalItems mockItems = mock(RentalItems.class);
    RentalItemTypes mockItemTypes = mock(RentalItemTypes.class);

    given(orderInfoService.isExistOrderInfo(rentalItemToken)).willReturn(false);
    given(rentalItemRepository.findByToken(rentalItemToken)).willReturn(mockItems);
    given(mockItems.getRentalItemType()).willReturn(mockItemTypes);
    given(mockItemTypes.getName()).willReturn(rentalItemName);
    given(mockItemTypes.getPrice()).willReturn(rentalItemPrice);

    //when
    PaymentInitializeResponse paymentRequest = paymentViewService.getPaymentRequest(rentalItemToken, rentalTime, paymentType);

    //then
    assertThat(paymentRequest).isNotNull()
                              .extracting("apiKey", "amount", "currency", "orderName")
                              .containsExactly("test", rentalItemPrice * rentalTime, "KRW", orderName);
    verify(orderInfoService).saveOrderInfo(
        anyString(), eq(rentalItemToken), eq(rentalTime), eq(rentalItemPrice * rentalTime), eq(paymentType)
    );
  }

  @Test
  @DisplayName("해당 물품에 대한 주문정보가 존재하면 예외 발생")
  void isExistOrderInfo() {
    //given
    String rentalItemToken = "rentalItemToken";
    Integer rentalTime = 1;
    PaymentType paymentType = PaymentType.RENT;
    given(orderInfoService.isExistOrderInfo(rentalItemToken)).willReturn(true);

    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> paymentViewService.getPaymentRequest(rentalItemToken, rentalTime, paymentType))
        .withMessage(ErrorCode.ORDER_INFO_EXISTS.getMessage());

    verify(rentalItemRepository, never()).findByToken(anyString());
    verify(orderInfoService, never()).saveOrderInfo(anyString(), eq(rentalItemToken), eq(rentalTime), anyInt(), eq(paymentType));
  }

  @Test
  @DisplayName("대여물품이 존재하지 않으면 예외 발생")
  void notFoundRentalItem() {
    //given
    String rentalItemToken = "rentalItemToken";
    Integer rentalTime = 1;
    PaymentType paymentType = PaymentType.RENT;
    given(orderInfoService.isExistOrderInfo(rentalItemToken)).willReturn(false);
    given(rentalItemRepository.findByToken(rentalItemToken))
        .willThrow(new BannabeServiceException(ErrorCode.RENTAL_ITEM_NOT_FOUND));

    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> paymentViewService.getPaymentRequest(rentalItemToken, rentalTime, paymentType))
        .withMessage(ErrorCode.RENTAL_ITEM_NOT_FOUND.getMessage());

    verify(orderInfoService, never()).saveOrderInfo(anyString(), eq(rentalItemToken), eq(rentalTime), anyInt(), eq(paymentType));
  }

}