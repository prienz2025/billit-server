package io.billit.server.domain.rentals.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import io.billit.server.domain.rentals.entity.RentalItems;
import io.billit.server.domain.rentals.repository.querydsl.CustomRentalItemRepository;

public interface RentalItemRepository extends JpaRepository<RentalItems, Long>, CustomRentalItemRepository {

}