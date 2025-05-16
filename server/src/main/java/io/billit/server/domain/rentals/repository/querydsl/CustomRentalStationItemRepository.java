package io.billit.server.domain.rentals.repository.querydsl;

import io.billit.server.domain.rentals.entity.RentalItemTypes;
import io.billit.server.domain.rentals.entity.RentalStationItems;
import io.billit.server.domain.rentals.entity.RentalStations;

public interface CustomRentalStationItemRepository {

  RentalStationItems findByItemTypeAndStation(RentalItemTypes rentalItemType, RentalStations rentalStation);

}