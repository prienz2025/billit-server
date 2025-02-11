package site.bannabe.server.domain.rentals.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import site.bannabe.server.config.AbstractTestContainers;
import site.bannabe.server.config.CustomDataJpaTest;
import site.bannabe.server.domain.rentals.controller.response.RentalStationDetailResponse.RentalItemResponse;
import site.bannabe.server.domain.rentals.entity.RentalItemCategory;
import site.bannabe.server.domain.rentals.entity.RentalItemTypes;
import site.bannabe.server.domain.rentals.entity.RentalStationItems;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.rentals.entity.StationGrade;
import site.bannabe.server.domain.rentals.entity.StationStatus;

@CustomDataJpaTest
class RentalStationRepositoryTest extends AbstractTestContainers {

  @Autowired
  private RentalStationRepository rentalStationRepository;

  @PersistenceContext
  private EntityManager em;

  @Test
  @DisplayName("스테이션 대여가능 물품 조회 테스트")
  void findRentalStationDetailBy() {
    //given
    RentalStations rentalStations = RentalStations.builder()
                                                  .name("테스트 스테이션")
                                                  .status(StationStatus.OPEN)
                                                  .grade(StationGrade.FIRST)
                                                  .build();
    em.persist(rentalStations);
    List<RentalItemTypes> rentalItemTypes = setupRentalItemTypes();
    List<RentalStationItems> rentalStationItems = rentalItemTypes.stream().map(itemType -> {
      RentalStationItems stationItems = new RentalStationItems(itemType, rentalStations, 10);
      em.persist(stationItems);
      return stationItems;
    }).toList();
    em.flush();
    em.clear();

    //when
    List<RentalItemResponse> results = rentalStationRepository.findRentalStationDetailBy(rentalStations.getId());

    //then
    assertThat(results).isNotNull().hasSize(rentalStationItems.size());
    for (int i = 0; i < results.size(); i++) {
      RentalItemResponse result = results.get(i);
      assertThat(result).isNotNull()
                        .extracting(
                            RentalItemResponse::itemTypeId,
                            RentalItemResponse::name,
                            RentalItemResponse::image,
                            RentalItemResponse::category,
                            RentalItemResponse::stock
                        ).containsExactly(
                            rentalItemTypes.get(i).getId(),
                            rentalItemTypes.get(i).getName(),
                            rentalItemTypes.get(i).getImage(),
                            rentalItemTypes.get(i).getCategory().name(),
                            rentalStationItems.get(i).getStock()
                        );
    }

  }


  private List<RentalItemTypes> setupRentalItemTypes() {
    List<RentalItemTypes> rentalItemTypes = new ArrayList<>();

    RentalItemCategory[] categories = RentalItemCategory.values();
    for (int i = 0; i < categories.length; i++) {
      RentalItemTypes rentalItemType = RentalItemTypes.builder()
                                                      .name(categories[i].getDescription())
                                                      .category(categories[i])
                                                      .image("test" + i + ".png")
                                                      .build();
      rentalItemTypes.add(rentalItemType);
      em.persist(rentalItemType);
    }

    return rentalItemTypes;
  }
}