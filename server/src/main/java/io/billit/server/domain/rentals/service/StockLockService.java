package io.billit.server.domain.rentals.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import io.billit.server.domain.rentals.entity.RentalItemTypes;
import io.billit.server.domain.rentals.entity.RentalItems;
import io.billit.server.domain.rentals.entity.RentalStationItems;
import io.billit.server.domain.rentals.entity.RentalStations;
import io.billit.server.domain.rentals.repository.RentalStationItemRepository;
import io.billit.server.global.aop.DistributedLock;

@Service
@RequiredArgsConstructor
public class StockLockService {

  private final RentalStationItemRepository rentalStationItemRepository;

  @DistributedLock(key = "'station:' + #rentalItem.getCurrentStation().getId() + ':itemType:' + #rentalItem.getRentalItemType().getId()")
  public void decreaseStock(RentalItems rentalItem) {
    RentalItemTypes rentalItemType = rentalItem.getRentalItemType();
    RentalStations currentStation = rentalItem.getCurrentStation();
    RentalStationItems rentalStationItem = rentalStationItemRepository.findByItemTypeAndStation(rentalItemType, currentStation);
    rentalStationItem.decreaseStock();
  }

  @DistributedLock(key = "'station:' + #rentalItem.getCurrentStation().getId() + ':itemType:' + #rentalItem.getRentalItemType().getId()")
  public void increaseStock(RentalItems rentalItem) {
    RentalItemTypes rentalItemType = rentalItem.getRentalItemType();
    RentalStations currentStation = rentalItem.getCurrentStation();
    RentalStationItems rentalStationItem = rentalStationItemRepository.findByItemTypeAndStation(rentalItemType, currentStation);
    rentalStationItem.increaseStock();
  }

}