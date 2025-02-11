package site.bannabe.server.domain.rentals.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.bannabe.server.domain.rentals.controller.response.ReturnItemDetailResponse;
import site.bannabe.server.domain.rentals.entity.RentalHistory;
import site.bannabe.server.domain.rentals.entity.RentalItemTypes;
import site.bannabe.server.domain.rentals.entity.RentalItems;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.rentals.entity.RentalStatus;
import site.bannabe.server.domain.rentals.repository.RentalStationRepository;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;

@ExtendWith(MockitoExtension.class)
class ReturnServiceTest {

  @InjectMocks
  private ReturnService returnService;

  @Mock
  private RentalHistoryService rentalHistoryService;
  @Mock
  private RentalStationRepository rentalStationRepository;
  @Mock
  private StockLockService stockLockService;

  @Test
  @DisplayName("반납 물품 데이터 조회 성공")
  void getReturnItemInfo() {
    //given
    String rentalItemToken = "rentalItemToken";
    Long currentStationId = 1L;
    LocalDateTime startTime = LocalDateTime.now().plusHours(1);
    int rentalTime = 1;
    String itemName = "충전기";
    String rentalStationName = "대여한 스테이션";
    String currentStationName = "현재 스테이션";

    RentalItems mockItem = mock(RentalItems.class);
    RentalItemTypes mockItemType = mock(RentalItemTypes.class);
    RentalStations rentalStation = mock(RentalStations.class);
    RentalStations currentStation = mock(RentalStations.class);

    RentalHistory rentalHistory = RentalHistory.builder()
                                               .status(RentalStatus.RENTAL)
                                               .rentalTimeHour(rentalTime)
                                               .startTime(startTime)
                                               .expectedReturnTime(startTime.plusHours(rentalTime))
                                               .rentalItem(mockItem)
                                               .rentalStation(rentalStation)
                                               .build();

    given(mockItem.getRentalItemType()).willReturn(mockItemType);
    given(mockItem.isRented()).willReturn(true);
    given(mockItemType.getName()).willReturn(itemName);
    given(rentalStation.getName()).willReturn(rentalStationName);
    given(currentStation.getName()).willReturn(currentStationName);
    given(rentalHistoryService.findRentalHistoryByRentalItemToken(rentalItemToken)).willReturn(rentalHistory);
    given(rentalStationRepository.findById(currentStationId)).willReturn(Optional.of(currentStation));

    //when
    ReturnItemDetailResponse result = returnService.getReturnItemInfo(rentalItemToken, currentStationId);

    //then
    assertThat(result).isNotNull()
                      .extracting(
                          ReturnItemDetailResponse::rentalItemName,
                          r -> r.rentalHistory().status(),
                          r -> r.rentalHistory().rentalTime(),
                          r -> r.rentalHistory().startTime(),
                          r -> r.rentalHistory().expectedReturnTime(),
                          r -> r.rentalStation().rentalStationName(),
                          r -> r.rentalStation().currentStationName()
                      )
                      .containsExactly(
                          itemName,
                          RentalStatus.RENTAL.name(),
                          rentalTime,
                          startTime,
                          startTime.plusHours(rentalTime),
                          rentalStationName,
                          currentStationName
                      );
  }

  @Test
  @DisplayName("대여하지 않은 물품일 시 예외 발생")
  void isNotRentedItem() {
    //given
    String rentalItemToken = "rentalItemToken";
    Long currentStationId = 1L;
    RentalItems mockItem = mock(RentalItems.class);
    RentalHistory mockHistory = mock(RentalHistory.class);

    given(mockHistory.getRentalItem()).willReturn(mockItem);
    given(mockItem.isRented()).willReturn(false);
    given(rentalHistoryService.findRentalHistoryByRentalItemToken(rentalItemToken)).willReturn(mockHistory);

    //when
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> returnService.getReturnItemInfo(rentalItemToken, currentStationId))
        .withMessage(ErrorCode.RENTAL_ITEM_NOT_RENTED.getMessage());

    verify(rentalStationRepository, never()).findById(currentStationId);
  }

  @Test
  @DisplayName("대여물품 반납 정상 작동")
  void returnRentalItem() {
    //given
    String rentalItemToken = "rentalItemToken";
    Long returnStationId = 1L;

    RentalItems mockItem = mock(RentalItems.class);
    RentalHistory mockHistory = mock(RentalHistory.class);

    RentalStations mockStation = mock(RentalStations.class);

    given(mockHistory.getRentalItem()).willReturn(mockItem);
    given(mockHistory.getStatus()).willReturn(RentalStatus.RENTAL);
    given(rentalHistoryService.findRentalHistoryByRentalItemToken(rentalItemToken)).willReturn(mockHistory);
    given(rentalStationRepository.findById(returnStationId)).willReturn(Optional.of(mockStation));

    //when
    returnService.returnRentalItem(rentalItemToken, returnStationId);

    //then
    verify(rentalHistoryService).findRentalHistoryByRentalItemToken(rentalItemToken);
    verify(rentalStationRepository).findById(returnStationId);
    verify(stockLockService).increaseStock(mockItem);
  }

  @Test
  @DisplayName("대여내역 상태가 연체일 시 예외 발생")
  void isOverDueHistory() {
    //given
    String rentalItemToken = "rentalItemToken";
    Long returnStationId = 1L;
    RentalHistory mockHistory = mock(RentalHistory.class);

    given(mockHistory.getStatus()).willReturn(RentalStatus.OVERDUE);
    given(rentalHistoryService.findRentalHistoryByRentalItemToken(rentalItemToken)).willReturn(mockHistory);

    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> returnService.returnRentalItem(rentalItemToken, returnStationId))
        .withMessage(ErrorCode.RENTAL_ITEM_OVERDUE.getMessage());

    verify(rentalStationRepository, never()).findById(returnStationId);
    verify(stockLockService, never()).increaseStock(any(RentalItems.class));
  }

}