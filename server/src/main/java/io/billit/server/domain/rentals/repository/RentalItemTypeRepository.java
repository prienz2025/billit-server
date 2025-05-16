package io.billit.server.domain.rentals.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import io.billit.server.domain.rentals.entity.RentalItemTypes;
import io.billit.server.domain.rentals.repository.querydsl.CustomRentalItemTypeRepository;

public interface RentalItemTypeRepository extends JpaRepository<RentalItemTypes, Long>, CustomRentalItemTypeRepository {

}