package site.bannabe.server.domain.rentals.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.transaction.annotation.Transactional;
import site.bannabe.server.config.AbstractTestContainers;
import site.bannabe.server.config.CustomDataJpaTest;
import site.bannabe.server.domain.payments.entity.PaymentMethod;
import site.bannabe.server.domain.payments.entity.PaymentStatus;
import site.bannabe.server.domain.payments.entity.PaymentType;
import site.bannabe.server.domain.payments.entity.RentalPayments;
import site.bannabe.server.domain.rentals.controller.response.RentalSuccessSimpleResponse;
import site.bannabe.server.domain.rentals.entity.RentalHistory;
import site.bannabe.server.domain.rentals.entity.RentalItemCategory;
import site.bannabe.server.domain.rentals.entity.RentalItemStatus;
import site.bannabe.server.domain.rentals.entity.RentalItemTypes;
import site.bannabe.server.domain.rentals.entity.RentalItems;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.rentals.entity.RentalStatus;
import site.bannabe.server.domain.rentals.entity.StationGrade;
import site.bannabe.server.domain.rentals.entity.StationStatus;
import site.bannabe.server.domain.users.entity.ProviderType;
import site.bannabe.server.domain.users.entity.Role;
import site.bannabe.server.domain.users.entity.Users;

@CustomDataJpaTest
class RentalHistoryRepositoryTest extends AbstractTestContainers {

  private final String EMAIL = "test@test.com";
  @Autowired
  private RentalHistoryRepository rentalHistoryRepository;
  @PersistenceContext
  private EntityManager em;

  private Users user;
  private List<RentalStations> rentalStations;
  private List<RentalItems> rentalItems;

  @BeforeEach
  void init() {
    user = Users.builder().email(EMAIL).providerType(ProviderType.LOCAL).role(Role.USER).build();
    em.persist(user);
    rentalStations = IntStream.range(0, 20)
                              .mapToObj(idx -> {
                                RentalStations stations = RentalStations.builder()
                                                                        .name("station" + idx)
                                                                        .status(StationStatus.OPEN)
                                                                        .grade(StationGrade.FIRST)
                                                                        .build();
                                em.persist(stations);
                                return stations;
                              })
                              .toList();

    rentalItems = IntStream.range(0, 20)
                           .mapToObj(idx -> {
                             RentalItems items = RentalItems.builder()
                                                            .status(RentalItemStatus.AVAILABLE)
                                                            .currentStation(rentalStations.get(idx))
                                                            .build();
                             em.persist(items);
                             return items;
                           }).toList();

    em.flush();
  }

  @Test
  @Transactional
  @DisplayName("대여현황 조회 테스트")
  void findActiveRentalsBy() {
    //given
    LocalDateTime now = LocalDateTime.now();
    List<RentalHistory> rentalHistories = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      RentalStatus status = i % 2 == 0 ? RentalStatus.RENTAL : RentalStatus.OVERDUE;
      rentalHistories.add(createRentalHistory(i, status, now));
    }
    rentalHistories.add(createRentalHistory(5, RentalStatus.RETURNED, now.minusDays(1)));
    rentalHistoryRepository.saveAllAndFlush(rentalHistories);

    //when
    List<RentalHistory> result = rentalHistoryRepository.findActiveRentalsBy(user.getToken());

    //then
    assertThat(result).isNotEmpty().hasSize(5)
                      .extracting(RentalHistory::getStatus)
                      .containsOnly(RentalStatus.RENTAL, RentalStatus.OVERDUE);
  }

  @Test
  @Transactional
  @DisplayName("전체 대여내역 조회 테스트")
  void findAllRentalsBy() {
    //given
    LocalDateTime now = LocalDateTime.now();
    List<RentalHistory> rentalHistories = new ArrayList<>();
    for (int i = 0; i < rentalItems.size(); i++) {
      RentalStatus status = switch (i % 3) {
        case 0 -> RentalStatus.RENTAL;
        case 1 -> RentalStatus.OVERDUE;
        default -> RentalStatus.RETURNED;
      };
      rentalHistories.add(createRentalHistory(i, status, now));
    }
    rentalHistoryRepository.saveAllAndFlush(rentalHistories);

    rentalHistories.sort(Comparator.comparing(RentalHistory::getStartTime).reversed());
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Direction.DESC, "startTime"));

    //when
    Page<RentalHistory> result = rentalHistoryRepository.findAllRentalsBy(user.getToken(), pageable);

    //then
    assertThat(result.getContent()).isNotEmpty().hasSize(10)
                                   .extracting(RentalHistory::getStartTime)
                                   .isSortedAccordingTo(Comparator.reverseOrder());

    for (int i = 0; i < 10; i++) {
      assertThat(result.getContent().get(i).getStartTime()).isEqualTo(rentalHistories.get(i).getStartTime());
    }

  }

  @Test
  @Transactional
  @DisplayName("대여 완료 데이터 조회 테스트")
  void findRentalHistoryInfoBy() {
    //given
    final String itemName = "충전기";
    final Integer totalAmount = 2000;
    String token = "token";
    final String stationName = "station0";
    final Integer rentalTimeHour = 2;
    setupRentalHistoryInfo(itemName, token, totalAmount, rentalTimeHour);

    //when
    RentalSuccessSimpleResponse result = rentalHistoryRepository.findRentalHistoryInfoBy("token");

    //then
    assertThat(result).isNotNull()
                      .extracting(
                          RentalSuccessSimpleResponse::totalAmount,
                          RentalSuccessSimpleResponse::itemName,
                          RentalSuccessSimpleResponse::rentalTime,
                          RentalSuccessSimpleResponse::rentalStationName
                      ).containsExactly(totalAmount, itemName, rentalTimeHour, stationName);
  }

  private void setupRentalHistoryInfo(String itemName, String token, Integer totalAmount, Integer rentalTimeHour) {
    RentalItemTypes rentalItemTypes = RentalItemTypes.builder()
                                                     .category(RentalItemCategory.CHARGER)
                                                     .name(itemName)
                                                     .price(1000)
                                                     .build();
    em.persist(rentalItemTypes);
    RentalItems rentalItems = RentalItems.builder().status(RentalItemStatus.RENTED).rentalItemType(rentalItemTypes).build();
    em.persist(rentalItems);

    RentalHistory rentalHistory = RentalHistory.builder()
                                               .status(RentalStatus.RENTAL)
                                               .rentalTimeHour(rentalTimeHour)
                                               .user(user)
                                               .token(token)
                                               .rentalItem(rentalItems)
                                               .rentalStation(rentalStations.get(0))
                                               .build();
    em.persist(rentalHistory);

    RentalPayments rentalPayments = RentalPayments.builder()
                                                  .totalAmount(totalAmount)
                                                  .rentalHistory(rentalHistory)
                                                  .status(PaymentStatus.APPROVED)
                                                  .paymentType(PaymentType.RENT)
                                                  .paymentMethod(PaymentMethod.CARD)
                                                  .build();
    em.persist(rentalPayments);
    em.flush();
    em.clear();
  }

  @Test
  @Transactional
  @DisplayName("대여물품 토큰 기반 대여내역 조회 테스트")
  void findByItemToken() {
    //given
    String rentalItemToken = "rentalItemToken";
    String itemName = "65W충전기";
    String stationName = "대여스테이션";
    LocalDateTime now = LocalDateTime.now();
    int rentalTimeHour = 2;
    RentalItemTypes itemType = RentalItemTypes.builder().name(itemName).category(RentalItemCategory.CHARGER).price(2000).build();
    em.persist(itemType);

    RentalItems rentalItem = RentalItems.builder()
                                        .status(RentalItemStatus.RENTED)
                                        .token(rentalItemToken)
                                        .rentalItemType(itemType)
                                        .build();

    em.persist(rentalItem);
    RentalStations station = RentalStations.builder()
                                           .name(stationName)
                                           .grade(StationGrade.FIRST)
                                           .status(StationStatus.OPEN)
                                           .build();
    em.persist(station);
    RentalHistory rentalHistory = RentalHistory.builder()
                                               .rentalItem(rentalItem)
                                               .user(user)
                                               .rentalTimeHour(rentalTimeHour)
                                               .startTime(now)
                                               .expectedReturnTime(now.plusHours(rentalTimeHour))
                                               .rentalStation(station)
                                               .build();
    em.persist(rentalHistory);
    em.flush();
    em.clear();

    //when
    RentalHistory result = rentalHistoryRepository.findByItemToken(rentalItemToken);

    //then
    assertThat(result).isNotNull()
                      .extracting(RentalHistory::getRentalTimeHour).isEqualTo(rentalTimeHour);
    assertThat(result.getRentalItem().getRentalItemType()).isNotNull()
                                                          .extracting(RentalItemTypes::getName)
                                                          .isEqualTo(itemName);
    assertThat(result.getRentalStation()).isNotNull()
                                         .extracting(RentalStations::getName)
                                         .isEqualTo(stationName);
  }

  private RentalHistory createRentalHistory(int index, RentalStatus status, LocalDateTime now) {
    return RentalHistory.builder()
                        .startTime(now.plusDays(index))
                        .rentalItem(rentalItems.get(index))
                        .rentalStation(rentalStations.get(index))
                        .status(status)
                        .user(user)
                        .build();
  }
}