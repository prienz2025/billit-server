package io.billit.server.domain.rentals.repository.querydsl;

import io.billit.server.domain.rentals.entity.RentalItems;

public interface CustomRentalItemRepository {

  RentalItems findByToken(String token);

  Integer findRentalItemPrice(String token);

}