package site.bannabe.server.domain.rentals.repository.querydsl;

import site.bannabe.server.domain.rentals.controller.response.RentalItemTypeDetailResponse;

public interface CustomRentalItemTypeRepository {

  RentalItemTypeDetailResponse findRentalItemDetailBy(Long stationId, Long itemTypeId);

}