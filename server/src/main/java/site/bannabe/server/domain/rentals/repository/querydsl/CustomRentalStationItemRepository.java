package site.bannabe.server.domain.rentals.repository.querydsl;

import site.bannabe.server.domain.rentals.entity.RentalItemTypes;
import site.bannabe.server.domain.rentals.entity.RentalStationItems;
import site.bannabe.server.domain.rentals.entity.RentalStations;

public interface CustomRentalStationItemRepository {

  RentalStationItems findByItemTypeAndStation(RentalItemTypes rentalItemType, RentalStations rentalStation);

}