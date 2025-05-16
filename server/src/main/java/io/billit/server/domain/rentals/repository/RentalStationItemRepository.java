package io.billit.server.domain.rentals.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import io.billit.server.domain.rentals.entity.RentalStationItems;
import io.billit.server.domain.rentals.repository.querydsl.CustomRentalStationItemRepository;

public interface RentalStationItemRepository extends JpaRepository<RentalStationItems, Long>, CustomRentalStationItemRepository {

}