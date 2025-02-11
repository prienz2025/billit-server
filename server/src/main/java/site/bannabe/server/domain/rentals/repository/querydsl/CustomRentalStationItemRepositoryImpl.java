package site.bannabe.server.domain.rentals.repository.querydsl;

import static site.bannabe.server.domain.rentals.entity.QRentalStationItems.rentalStationItems;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import site.bannabe.server.domain.rentals.entity.RentalItemTypes;
import site.bannabe.server.domain.rentals.entity.RentalStationItems;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;

@Repository
@RequiredArgsConstructor
public class CustomRentalStationItemRepositoryImpl implements CustomRentalStationItemRepository {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public RentalStationItems findByItemTypeAndStation(RentalItemTypes rentalItemType, RentalStations rentalStation) {
    RentalStationItems result = jpaQueryFactory.selectFrom(rentalStationItems)
                                               .where(rentalStationItems.rentalItemType.eq(rentalItemType)
                                                                                       .and(rentalStationItems.rentalStation.eq(
                                                                                           rentalStation)))
                                               .fetchOne();
    return Optional.ofNullable(result).orElseThrow(() -> new BannabeServiceException(ErrorCode.RENTAL_STATION_ITEM_NOT_FOUND));
  }

}