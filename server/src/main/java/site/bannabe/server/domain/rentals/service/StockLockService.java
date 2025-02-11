package site.bannabe.server.domain.rentals.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.bannabe.server.domain.rentals.entity.RentalItemTypes;
import site.bannabe.server.domain.rentals.entity.RentalItems;
import site.bannabe.server.domain.rentals.entity.RentalStationItems;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.rentals.repository.RentalStationItemRepository;
import site.bannabe.server.global.aop.DistributedLock;

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