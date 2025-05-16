package io.billit.server.domain.rentals.repository.querydsl;

import java.util.List;
import io.billit.server.domain.rentals.controller.response.RentalStationDetailResponse.RentalItemResponse;

public interface CustomRentalStationRepository {

  List<RentalItemResponse> findRentalStationDetailBy(Long stationId);

}