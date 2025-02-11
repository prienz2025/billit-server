package site.bannabe.server.domain.rentals.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.rentals.repository.querydsl.CustomRentalStationRepository;

public interface RentalStationRepository extends JpaRepository<RentalStations, Long>, CustomRentalStationRepository {

}