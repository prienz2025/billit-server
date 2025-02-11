package site.bannabe.server.domain.rentals.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.bannabe.server.domain.rentals.entity.RentalHistory;
import site.bannabe.server.domain.rentals.repository.querydsl.CustomRentalHistoryRepository;

public interface RentalHistoryRepository extends JpaRepository<RentalHistory, Long>, CustomRentalHistoryRepository {

}