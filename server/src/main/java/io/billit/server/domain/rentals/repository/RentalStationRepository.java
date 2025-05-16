package io.billit.server.domain.rentals.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import io.billit.server.domain.rentals.entity.RentalStations;
import io.billit.server.domain.rentals.repository.querydsl.CustomRentalStationRepository;

public interface RentalStationRepository extends JpaRepository<RentalStations, Long>, CustomRentalStationRepository {

}