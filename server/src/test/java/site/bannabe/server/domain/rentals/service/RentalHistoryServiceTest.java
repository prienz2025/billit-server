package site.bannabe.server.domain.rentals.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.bannabe.server.domain.rentals.entity.RentalHistory;
import site.bannabe.server.domain.rentals.entity.RentalItems;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.rentals.repository.RentalHistoryRepository;
import site.bannabe.server.domain.users.entity.Users;
import site.bannabe.server.global.type.OrderInfo;

@ExtendWith(MockitoExtension.class)
class RentalHistoryServiceTest {

  @InjectMocks
  private RentalHistoryService rentalHistoryService;

  @Mock
  private RentalHistoryRepository rentalHistoryRepository;

  @Test
  @DisplayName("rentalItemToken으로 대여내역을 조회한다.")
  void findRentalHistoryByRentalItemToken() {
    //given
    String rentalItemToken = "rentalItemToken";
    RentalHistory mockHistory = mock(RentalHistory.class);

    given(rentalHistoryRepository.findByItemToken(rentalItemToken)).willReturn(mockHistory);

    //when
    RentalHistory result = rentalHistoryService.findRentalHistoryByRentalItemToken(rentalItemToken);

    //then
    assertThat(result).isEqualTo(mockHistory);
    verify(rentalHistoryRepository).findByItemToken(rentalItemToken);
  }

  @Test
  @DisplayName("대여 물품, 회원, 주문정보, 시작시간을 입력받아 대여내역을 저장한다.")
  void saveRentalHistory() {
    //given
    RentalItems mockItems = mock(RentalItems.class);
    Users mockUser = mock(Users.class);
    OrderInfo mockOrderInfo = mock(OrderInfo.class);
    LocalDateTime startTime = LocalDateTime.now();
    RentalHistory expected = RentalHistory.create(mockItems, mockUser, mockOrderInfo, startTime);

    given(mockOrderInfo.getRentalTime()).willReturn(1);
    given(mockItems.getCurrentStation()).willReturn(mock(RentalStations.class));
    given(rentalHistoryRepository.save(any(RentalHistory.class))).willReturn(expected);

    //when
    RentalHistory result = rentalHistoryService.saveRentalHistory(mockItems, mockUser, mockOrderInfo, startTime);

    //then
    assertThat(result).isEqualTo(expected);
  }

}