package site.bannabe.server.domain.rentals.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.bannabe.server.domain.rentals.entity.RentalStationItems;
import site.bannabe.server.domain.rentals.repository.querydsl.CustomRentalStationItemRepository;

public interface RentalStationItemRepository extends JpaRepository<RentalStationItems, Long>, CustomRentalStationItemRepository {

}