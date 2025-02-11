package site.bannabe.server.domain.rentals.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.bannabe.server.domain.rentals.controller.response.RentalItemDetailResponse;
import site.bannabe.server.domain.rentals.entity.RentalItemStatus;
import site.bannabe.server.domain.rentals.entity.RentalItemTypes;
import site.bannabe.server.domain.rentals.entity.RentalItems;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.rentals.repository.RentalItemRepository;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

  @InjectMocks
  private RentalService rentalService;

  @Mock
  private RentalItemRepository rentalItemRepository;

  @Test
  @DisplayName("대여 아이템 정보 조회 성공")
  void getRentalItemInfo() {
    //given
    String rentalItemToken = "rentalItemToken";
    String itemName = "Rental Item";
    int price = 1000;
    String stationName = "테스트 스테이션";
    RentalItemTypes mockItemType = mock(RentalItemTypes.class);
    RentalStations mockStation = mock(RentalStations.class);

    RentalItems rentalItem = RentalItems.builder()
                                        .rentalItemType(mockItemType)
                                        .token(rentalItemToken)
                                        .status(RentalItemStatus.AVAILABLE)
                                        .currentStation(mockStation)
                                        .build();

    given(mockItemType.getName()).willReturn(itemName);
    given(mockItemType.getPrice()).willReturn(price);
    given(mockStation.getName()).willReturn(stationName);
    given(rentalItemRepository.findByToken(rentalItemToken)).willReturn(rentalItem);

    //when
    RentalItemDetailResponse result = rentalService.getRentalItemInfo(rentalItemToken);

    //then
    assertThat(result).isNotNull()
                      .extracting(
                          RentalItemDetailResponse::name,
                          RentalItemDetailResponse::price,
                          RentalItemDetailResponse::currentStationName
                      ).containsExactly(itemName, price, stationName);
  }

  @Test
  @DisplayName("이미 대여 중인 물품일 시 예외 발생")
  void isRentedItem() {
    //given
    String rentalItemToken = "rentalItemToken";
    RentalItems item = RentalItems.builder().status(RentalItemStatus.RENTED).build();

    given(rentalItemRepository.findByToken(rentalItemToken)).willReturn(item);

    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> rentalService.getRentalItemInfo(rentalItemToken))
        .withMessage(ErrorCode.RENTAL_ITEM_ALREADY_RENTED.getMessage());
  }
}