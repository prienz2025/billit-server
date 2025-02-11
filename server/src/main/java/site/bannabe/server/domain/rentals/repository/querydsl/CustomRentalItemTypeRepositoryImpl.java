package site.bannabe.server.domain.rentals.repository.querydsl;

import static site.bannabe.server.domain.rentals.entity.QRentalItemTypes.rentalItemTypes;
import static site.bannabe.server.domain.rentals.entity.QRentalStationItems.rentalStationItems;
import static site.bannabe.server.domain.rentals.entity.QRentalStations.rentalStations;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import site.bannabe.server.domain.rentals.controller.response.RentalItemTypeDetailResponse;

@Repository
@RequiredArgsConstructor
public class CustomRentalItemTypeRepositoryImpl implements CustomRentalItemTypeRepository {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public RentalItemTypeDetailResponse findRentalItemDetailBy(Long stationId, Long itemTypeId) {
    return jpaQueryFactory.select(Projections.constructor(
                              RentalItemTypeDetailResponse.class,
                              rentalItemTypes.name,
                              rentalItemTypes.image,
                              rentalItemTypes.category.stringValue(),
                              rentalItemTypes.description,
                              rentalItemTypes.price,
                              rentalStationItems.stock
                          ))
                          .from(rentalStationItems)
                          .join(rentalStationItems.rentalStation, rentalStations)
                          .join(rentalStationItems.rentalItemType, rentalItemTypes)
                          .where(rentalStations.id.eq(stationId)
                                                  .and(rentalItemTypes.id.eq(itemTypeId)))
                          .fetchOne();
  }

}