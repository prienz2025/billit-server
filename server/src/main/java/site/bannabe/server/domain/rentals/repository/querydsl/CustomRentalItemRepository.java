package site.bannabe.server.domain.rentals.repository.querydsl;

import site.bannabe.server.domain.rentals.entity.RentalItems;

public interface CustomRentalItemRepository {

  RentalItems findByToken(String token);

  Integer findRentalItemPrice(String token);

}