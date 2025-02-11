package site.bannabe.server.domain.rentals.repository.querydsl;

import static site.bannabe.server.domain.rentals.entity.QRentalItemTypes.rentalItemTypes;
import static site.bannabe.server.domain.rentals.entity.QRentalStationItems.rentalStationItems;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import site.bannabe.server.domain.rentals.controller.response.RentalStationDetailResponse.RentalItemResponse;

@Repository
@RequiredArgsConstructor
public class CustomRentalStationRepositoryImpl implements CustomRentalStationRepository {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public List<RentalItemResponse> findRentalStationDetailBy(Long stationId) {
    return jpaQueryFactory.select(Projections.constructor(
                              RentalItemResponse.class,
                              rentalItemTypes.id,
                              rentalItemTypes.name,
                              rentalItemTypes.image,
                              rentalItemTypes.category.stringValue(),
                              rentalStationItems.stock
                          ))
                          .from(rentalStationItems)
                          .join(rentalStationItems.rentalItemType, rentalItemTypes)
                          .where(rentalStationItems.rentalStation.id.eq(stationId))
                          .fetch();
  }

}