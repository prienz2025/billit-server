package site.bannabe.server.domain.rentals.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import site.bannabe.server.config.AbstractTestContainers;
import site.bannabe.server.config.CustomDataJpaTest;
import site.bannabe.server.domain.rentals.controller.response.RentalItemTypeDetailResponse;
import site.bannabe.server.domain.rentals.entity.RentalItemCategory;
import site.bannabe.server.domain.rentals.entity.RentalItemTypes;
import site.bannabe.server.domain.rentals.entity.RentalStationItems;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.rentals.entity.StationGrade;
import site.bannabe.server.domain.rentals.entity.StationStatus;

@Slf4j
@CustomDataJpaTest
class RentalItemTypeRepositoryTest extends AbstractTestContainers {

  @Autowired
  private RentalItemTypeRepository rentalItemTypeRepository;

  @PersistenceContext
  private EntityManager em;

  @Test
  @DisplayName("StationId와 ItemTypeID로 물품 상세정보 조회 테스트")
  void findRentalItemDetailBy() {
    //given
    RentalItemTypes rentalItemTypes = RentalItemTypes.builder()
                                                     .name("65W충전기")
                                                     .image("test.png")
                                                     .category(RentalItemCategory.CHARGER)
                                                     .description("PPS 3.0 PD 충전 가능")
                                                     .price(1000)
                                                     .build();

    RentalStations rentalStations = RentalStations.builder()
                                                  .name("테스트 대여스테이션")
                                                  .status(StationStatus.OPEN)
                                                  .grade(StationGrade.FIRST)
                                                  .build();
    em.persist(rentalItemTypes);
    em.persist(rentalStations);

    RentalStationItems rentalStationItems = new RentalStationItems(rentalItemTypes, rentalStations, 30);
    em.persist(rentalStationItems);

    Long stationId = rentalStations.getId();
    Long itemTypeId = rentalItemTypes.getId();

    //when
    RentalItemTypeDetailResponse result = rentalItemTypeRepository.findRentalItemDetailBy(stationId, itemTypeId);
    
    //then

    assertThat(result).isNotNull().extracting(
        RentalItemTypeDetailResponse::name,
        RentalItemTypeDetailResponse::image,
        RentalItemTypeDetailResponse::category,
        RentalItemTypeDetailResponse::description,
        RentalItemTypeDetailResponse::price,
        RentalItemTypeDetailResponse::stock
    ).containsExactly(
        rentalItemTypes.getName(),
        rentalItemTypes.getImage(),
        rentalItemTypes.getCategory().name(),
        rentalItemTypes.getDescription(),
        rentalItemTypes.getPrice(),
        rentalStationItems.getStock()
    );
  }
}