package site.bannabe.server.domain.rentals.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import site.bannabe.server.domain.rentals.entity.RentalItemTypes;
import site.bannabe.server.domain.rentals.entity.RentalItems;
import site.bannabe.server.domain.rentals.entity.RentalStationItems;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.rentals.repository.RentalStationItemRepository;

@Service
public class TestRentalStockService {

  private final RentalStationItemRepository rentalStationItemRepository;

  public TestRentalStockService(RentalStationItemRepository rentalStationItemRepository) {
    this.rentalStationItemRepository = rentalStationItemRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  void decreaseStock(RentalItems rentalItems) {
    RentalItemTypes rentalItemType = rentalItems.getRentalItemType();
    RentalStations currentStation = rentalItems.getCurrentStation();
    RentalStationItems rentalStationItem = rentalStationItemRepository.findByItemTypeAndStation(rentalItemType, currentStation);
    rentalStationItem.decreaseStock();
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  void increaseStock(RentalItems rentalItems) {
    RentalItemTypes rentalItemType = rentalItems.getRentalItemType();
    RentalStations currentStation = rentalItems.getCurrentStation();
    RentalStationItems rentalStationItem = rentalStationItemRepository.findByItemTypeAndStation(rentalItemType, currentStation);
    rentalStationItem.increaseStock();
  }

}