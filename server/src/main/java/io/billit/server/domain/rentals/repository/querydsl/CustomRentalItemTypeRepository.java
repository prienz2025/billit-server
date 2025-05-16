package io.billit.server.domain.rentals.repository.querydsl;

import io.billit.server.domain.rentals.controller.response.RentalItemTypeDetailResponse;

public interface CustomRentalItemTypeRepository {

  RentalItemTypeDetailResponse findRentalItemDetailBy(Long stationId, Long itemTypeId);

}