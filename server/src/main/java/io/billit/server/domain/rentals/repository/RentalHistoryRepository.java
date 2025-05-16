package io.billit.server.domain.rentals.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import io.billit.server.domain.rentals.entity.RentalHistory;
import io.billit.server.domain.rentals.repository.querydsl.CustomRentalHistoryRepository;

public interface RentalHistoryRepository extends JpaRepository<RentalHistory, Long>, CustomRentalHistoryRepository {

}