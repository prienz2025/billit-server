package site.bannabe.server.domain.rentals.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import site.bannabe.server.config.AbstractTestContainers;
import site.bannabe.server.domain.rentals.entity.RentalItemCategory;
import site.bannabe.server.domain.rentals.entity.RentalItemTypes;
import site.bannabe.server.domain.rentals.entity.RentalItems;
import site.bannabe.server.domain.rentals.entity.RentalStationItems;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.rentals.entity.StationGrade;
import site.bannabe.server.domain.rentals.entity.StationStatus;
import site.bannabe.server.domain.rentals.repository.RentalItemRepository;
import site.bannabe.server.domain.rentals.repository.RentalItemTypeRepository;
import site.bannabe.server.domain.rentals.repository.RentalStationItemRepository;
import site.bannabe.server.domain.rentals.repository.RentalStationRepository;

@Slf4j
@SpringBootTest
class StockLockServiceTest extends AbstractTestContainers {

  private static final String TOKEN = "token";
  private static final int STOCK = 100;
  @Autowired
  private StockLockService stockLockService;
  @Autowired
  private TestRentalStockService testRentalStockService;
  @Autowired
  private RentalStationItemRepository rentalStationItemRepository;
  @Autowired
  private RentalItemRepository rentalItemRepository;
  @Autowired
  private RentalItemTypeRepository rentalItemTypeRepository;
  @Autowired
  private RentalStationRepository rentalStationRepository;
  private RentalItems rentalItems;

  @BeforeEach
  void init() {
    RentalItemTypes rentalItemTypes = RentalItemTypes.builder()
                                                     .category(RentalItemCategory.CHARGER)
                                                     .name("Test Item Name")
                                                     .build();
    RentalStations rentalStations = RentalStations.builder()
                                                  .name("Test Station Name")
                                                  .status(StationStatus.OPEN)
                                                  .grade(StationGrade.FIRST)
                                                  .build();
    rentalItemTypeRepository.saveAndFlush(rentalItemTypes);
    rentalStationRepository.saveAndFlush(rentalStations);

    rentalItems = RentalItems.builder()
                             .token(TOKEN)
                             .rentalItemType(rentalItemTypes)
                             .currentStation(rentalStations)
                             .build();

    RentalStationItems rentalStationItems = new RentalStationItems(rentalItemTypes, rentalStations, STOCK);
    rentalItemRepository.saveAndFlush(rentalItems);
    rentalStationItemRepository.saveAndFlush(rentalStationItems);
  }

  @AfterEach
  void tearDown() {
    rentalStationItemRepository.deleteAllInBatch();
    rentalItemRepository.deleteAllInBatch();
    rentalItemTypeRepository.deleteAllInBatch();
    rentalStationRepository.deleteAllInBatch();
  }

  @Test
  @DisplayName("재고 감소 분산락 적용 테스트")
  void decreaseStockWithDistributedLock() throws InterruptedException {
    //given when
    executeConcurrentStockOperation(() -> stockLockService.decreaseStock(rentalItems));

    RentalStationItems rentalStationItems = rentalStationItemRepository.findByItemTypeAndStation(rentalItems.getRentalItemType(),
        rentalItems.getCurrentStation());

    //then
    assertThat(rentalStationItems.getStock()).isZero();
  }

  @Test
  @DisplayName("재고 감소 분산락 미적용 테스트")
  void decreaseStockWithOutDistributedLock() throws InterruptedException {
    //given when
    executeConcurrentStockOperation(() -> testRentalStockService.decreaseStock(rentalItems));

    RentalStationItems rentalStationItems = rentalStationItemRepository.findByItemTypeAndStation(rentalItems.getRentalItemType(),
        rentalItems.getCurrentStation());

    //then
    assertThat(rentalStationItems.getStock()).isNotZero();
  }

  @Test
  @DisplayName("재고 증가 분산락 적용 테스트")
  void increaseStockWithDistributedLock() throws InterruptedException {
    //given when
    executeConcurrentStockOperation(() -> stockLockService.increaseStock(rentalItems));
    RentalStationItems result = rentalStationItemRepository.findByItemTypeAndStation(rentalItems.getRentalItemType(),
        rentalItems.getCurrentStation());

    //then
    assertThat(result.getStock()).isEqualTo(STOCK + 100);
  }

  @Test
  @DisplayName("재고 증가 분산락 미적용 테스트")
  void increaseStockWithOutDistributedLock() throws InterruptedException {
    //given when
    executeConcurrentStockOperation(() -> testRentalStockService.increaseStock(rentalItems));

    RentalStationItems result = rentalStationItemRepository.findByItemTypeAndStation(rentalItems.getRentalItemType(),
        rentalItems.getCurrentStation());

    //then
    assertThat(result.getStock()).isNotEqualTo(STOCK + 100);
  }

  private void executeConcurrentStockOperation(Runnable stockOperation) throws InterruptedException {
    int threadSize = 100;
    ExecutorService executorService = Executors.newFixedThreadPool(threadSize);
    CountDownLatch latch = new CountDownLatch(threadSize);

    for (int i = 0; i < threadSize; i++) {
      executorService.execute(() -> {
        try {
          stockOperation.run();
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
  }

}