package site.bannabe.server.domain.rentals.service;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.rentals.repository.RentalStationRepository;
import site.bannabe.server.domain.users.entity.BookmarkStations;
import site.bannabe.server.domain.users.entity.Users;
import site.bannabe.server.domain.users.repository.BookmarkStationRepository;
import site.bannabe.server.domain.users.repository.UserRepository;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;

@ExtendWith(MockitoExtension.class)
class RentalStationServiceTest {

  private final Long stationId = 1L;
  private final String entityToken = "entityToken";
  private final Users mockUser = mock(Users.class);
  private final RentalStations mockStation = mock(RentalStations.class);

  @InjectMocks
  private RentalStationService rentalStationService;
  @Mock
  private RentalStationRepository rentalStationRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private BookmarkStationRepository bookmarkStationRepository;

  @Test
  @DisplayName("북마크 추가 성공")
  void bookmarkRentalStation() {
    //given
    given(userRepository.findByToken(entityToken)).willReturn(mockUser);
    given(rentalStationRepository.findById(stationId)).willReturn(Optional.of(mockStation));
    given(bookmarkStationRepository.existsByUserAndStation(mockUser, mockStation)).willReturn(false);

    //when
    rentalStationService.bookmarkRentalStation(stationId, entityToken);

    //then
    verify(bookmarkStationRepository).save(any(BookmarkStations.class));
  }

  @Test
  @DisplayName("스테이션 정보가 존재하지 않으면 예외 발생")
  void notFoundRentalStation() {
    //given
    given(userRepository.findByToken(entityToken)).willReturn(mockUser);
    given(rentalStationRepository.findById(stationId)).willReturn(Optional.empty());

    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> rentalStationService.bookmarkRentalStation(stationId, entityToken))
        .withMessage(ErrorCode.RENTAL_STATION_NOT_FOUND.getMessage());

    verify(bookmarkStationRepository, never()).existsByTokenAndId(entityToken, stationId);
    verify(bookmarkStationRepository, never()).save(any(BookmarkStations.class));
  }

  @Test
  @DisplayName("이미 북마크한 스테이션이면 예외 발생")
  void alreadyBookmarked() {
    //given
    given(userRepository.findByToken(entityToken)).willReturn(mockUser);
    given(rentalStationRepository.findById(stationId)).willReturn(Optional.of(mockStation));
    given(bookmarkStationRepository.existsByUserAndStation(mockUser, mockStation)).willReturn(true);

    //when
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> rentalStationService.bookmarkRentalStation(stationId, entityToken))
        .withMessage(ErrorCode.ALREADY_BOOKMARKED.getMessage());

    verify(bookmarkStationRepository, never()).save(any(BookmarkStations.class));
  }

}