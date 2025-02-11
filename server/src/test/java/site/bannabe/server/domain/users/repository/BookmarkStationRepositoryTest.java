package site.bannabe.server.domain.users.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import site.bannabe.server.config.AbstractTestContainers;
import site.bannabe.server.config.CustomDataJpaTest;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.rentals.entity.StationGrade;
import site.bannabe.server.domain.rentals.entity.StationStatus;
import site.bannabe.server.domain.users.controller.response.UserBookmarkStationsResponse.BookmarkStationResponse;
import site.bannabe.server.domain.users.entity.BookmarkStations;
import site.bannabe.server.domain.users.entity.ProviderType;
import site.bannabe.server.domain.users.entity.Role;
import site.bannabe.server.domain.users.entity.Users;

@CustomDataJpaTest
class BookmarkStationRepositoryTest extends AbstractTestContainers {

  @Autowired
  private BookmarkStationRepository bookmarkStationRepository;

  @PersistenceContext
  private EntityManager em;

  private Users user;
  private List<BookmarkStations> bookmarkStations;

  @BeforeEach
  void init() {
    user = Users.builder().email("test@test.com").token("test-token").providerType(ProviderType.LOCAL).role(Role.USER).build();
    em.persist(user);
    List<RentalStations> rentalStations = IntStream.range(0, 10).mapToObj(i -> {
      RentalStations station = RentalStations.builder()
                                             .name("station" + i)
                                             .status(StationStatus.OPEN)
                                             .grade(StationGrade.FIRST)
                                             .build();
      em.persist(station);
      return station;
    }).toList();

    bookmarkStations = rentalStations.stream().map(station -> {
      BookmarkStations bookmarkStations = new BookmarkStations(user, station);
      em.persist(bookmarkStations);
      return bookmarkStations;
    }).collect(Collectors.toList());
  }

  @Test
  @DisplayName("북마크 스테이션 조회 테스트")
  void findBookmarkStationsBy() {
    //given
    bookmarkStations.sort(Comparator.comparing(bookmark -> bookmark.getRentalStation().getName()));
    //when
    List<BookmarkStationResponse> results = bookmarkStationRepository.findBookmarkStationsBy(user.getToken());

    //then
    assertThat(results).hasSize(bookmarkStations.size());
    for (int i = 0; i < results.size(); i++) {
      BookmarkStationResponse result = results.get(i);
      BookmarkStations bookmark = bookmarkStations.get(i);
      assertThat(result).isNotNull()
                        .extracting(
                            BookmarkStationResponse::name,
                            BookmarkStationResponse::stationId,
                            BookmarkStationResponse::bookmarkId
                        ).containsExactly(
                            bookmark.getRentalStation().getName(),
                            bookmark.getRentalStation().getId(),
                            bookmark.getId()
                        );
    }
  }


  @Test
  @DisplayName("이메일 기반 북마크 스테이션 중복 체크 테스트")
  void existsBookmarkByEmail() {
    //given
    RentalStations rentalStation = setupNotExistRentalStation();
    //when
    boolean isExist = bookmarkStationRepository.existsByTokenAndId(user.getToken(), bookmarkStations.get(0).getId());
    boolean isNotExist = bookmarkStationRepository.existsByTokenAndId(user.getToken(), rentalStation.getId());
    //then
    assertThat(isExist).isTrue();
    assertThat(isNotExist).isFalse();
  }


  @Test
  @DisplayName("Entity 기반 북마크 스테이션 중복 체크 테스트")
  void existsBookmarkByEmailAndStation() {
    //given
    RentalStations rentalStation = setupNotExistRentalStation();
    //when
    boolean isExist = bookmarkStationRepository.existsByUserAndStation(user, bookmarkStations.get(0).getRentalStation());
    boolean isNotExist = bookmarkStationRepository.existsByUserAndStation(user, rentalStation);

    //then
    assertThat(isExist).isTrue();
    assertThat(isNotExist).isFalse();
  }

  @Test
  @DisplayName("북마크 삭제 테스트")
  void deleteBookmarkById() {
    //given when
    bookmarkStationRepository.deleteBookmarkById(bookmarkStations.get(0).getId());

    List<BookmarkStationResponse> bookmarkStations = bookmarkStationRepository.findBookmarkStationsBy(user.getToken());
    //then
    assertThat(bookmarkStations).hasSize(9);
  }

  private RentalStations setupNotExistRentalStation() {
    RentalStations rentalStation = RentalStations.builder()
                                                 .name("북마크 미지정 스테이션")
                                                 .status(StationStatus.OPEN)
                                                 .grade(StationGrade.FIRST)
                                                 .build();
    em.persist(rentalStation);
    em.flush();
    em.clear();
    return rentalStation;
  }

}