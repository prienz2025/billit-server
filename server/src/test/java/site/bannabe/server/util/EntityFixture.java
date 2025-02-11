package site.bannabe.server.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import site.bannabe.server.domain.payments.entity.PaymentMethod;
import site.bannabe.server.domain.payments.entity.PaymentStatus;
import site.bannabe.server.domain.payments.entity.PaymentType;
import site.bannabe.server.domain.payments.entity.RentalPayments;
import site.bannabe.server.domain.payments.repository.RentalPaymentRepository;
import site.bannabe.server.domain.rentals.entity.RentalHistory;
import site.bannabe.server.domain.rentals.entity.RentalItemCategory;
import site.bannabe.server.domain.rentals.entity.RentalItemStatus;
import site.bannabe.server.domain.rentals.entity.RentalItemTypes;
import site.bannabe.server.domain.rentals.entity.RentalItems;
import site.bannabe.server.domain.rentals.entity.RentalStationItems;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.rentals.entity.RentalStatus;
import site.bannabe.server.domain.rentals.entity.StationGrade;
import site.bannabe.server.domain.rentals.entity.StationStatus;
import site.bannabe.server.domain.rentals.repository.RentalHistoryRepository;
import site.bannabe.server.domain.rentals.repository.RentalItemRepository;
import site.bannabe.server.domain.rentals.repository.RentalItemTypeRepository;
import site.bannabe.server.domain.rentals.repository.RentalStationItemRepository;
import site.bannabe.server.domain.rentals.repository.RentalStationRepository;
import site.bannabe.server.domain.users.entity.BookmarkStations;
import site.bannabe.server.domain.users.entity.ProviderType;
import site.bannabe.server.domain.users.entity.Role;
import site.bannabe.server.domain.users.entity.Users;
import site.bannabe.server.domain.users.repository.BookmarkStationRepository;
import site.bannabe.server.domain.users.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class EntityFixture {

  private final RentalPaymentRepository rentalPaymentRepository;
  private final RentalHistoryRepository rentalHistoryRepository;
  private final RentalItemRepository rentalItemRepository;
  private final RentalItemTypeRepository rentalItemTypeRepository;
  private final RentalStationItemRepository rentalStationItemRepository;
  private final RentalStationRepository rentalStationRepository;
  private final BookmarkStationRepository bookmarkStationRepository;
  private final UserRepository userRepository;

  public void clearAll() {
    rentalPaymentRepository.deleteAllInBatch();
    rentalHistoryRepository.deleteAllInBatch();
    rentalItemRepository.deleteAllInBatch();
    rentalStationItemRepository.deleteAllInBatch();
    bookmarkStationRepository.deleteAllInBatch();
    rentalItemTypeRepository.deleteAllInBatch();
    rentalStationRepository.deleteAllInBatch();
    userRepository.deleteAllInBatch();
  }

  public Users createUser(String email) {
    Users user = Users.builder()
                      .email(email)
                      .password("password")
                      .providerType(ProviderType.LOCAL)
                      .role(Role.USER)
                      .build();
    return userRepository.saveAndFlush(user);
  }

  public RentalItemTypes createItemType(String name, int price, RentalItemCategory category) {
    RentalItemTypes itemType = RentalItemTypes.builder()
                                              .name(name)
                                              .price(price)
                                              .category(category)
                                              .image("test-image.png")
                                              .description("테스트 설명")
                                              .build();
    return rentalItemTypeRepository.saveAndFlush(itemType);
  }

  public RentalStations createStation(String name) {
    RentalStations station = RentalStations.builder()
                                           .name(name)
                                           .status(StationStatus.OPEN)
                                           .grade(StationGrade.FIRST)
                                           .openTime("09:00")
                                           .closeTime("18:00")
                                           .address("서울시 강남구")
                                           .latitude(BigDecimal.valueOf(37.514575))
                                           .longitude(BigDecimal.valueOf(127.049555))
                                           .build();
    return rentalStationRepository.saveAndFlush(station);
  }

  public RentalItems createItem(RentalItemStatus status, RentalStations station, RentalItemTypes itemType) {
    RentalItems item = RentalItems.builder()
                                  .currentStation(station)
                                  .rentalItemType(itemType)
                                  .status(status)
                                  .build();
    return rentalItemRepository.saveAndFlush(item);
  }

  public RentalStationItems createStationItem(RentalItemTypes itemType, RentalStations station) {
    RentalStationItems stationItem = new RentalStationItems(itemType, station, 10);
    return rentalStationItemRepository.saveAndFlush(stationItem);
  }

  public BookmarkStations createBookmark(Users user, RentalStations station) {
    BookmarkStations bookmark = new BookmarkStations(user, station);
    return bookmarkStationRepository.saveAndFlush(bookmark);
  }

  public RentalPayments createPayment(PaymentType type, String name, Integer totalAmount, RentalHistory rentalHistory) {
    RentalPayments rentalPayments = RentalPayments.builder()
                                                  .orderName(name)
                                                  .paymentMethod(PaymentMethod.CARD)
                                                  .status(PaymentStatus.APPROVED)
                                                  .paymentType(type)
                                                  .rentalHistory(rentalHistory)
                                                  .totalAmount(totalAmount)
                                                  .build();
    return rentalPaymentRepository.saveAndFlush(rentalPayments);
  }

  public RentalHistory createRentalHistory(RentalStatus status, Users user, RentalItems item, RentalStations station,
      LocalDateTime startTime) {
    RentalHistory rentalHistory = RentalHistory.builder()
                                               .user(user)
                                               .rentalItem(item)
                                               .rentalStation(station)
                                               .startTime(startTime)
                                               .rentalTimeHour(1)
                                               .expectedReturnTime(startTime.plusHours(1))
                                               .status(status)
                                               .build();
    return rentalHistoryRepository.saveAndFlush(rentalHistory);
  }
}