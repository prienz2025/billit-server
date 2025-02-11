package site.bannabe.server.domain.rentals.repository.querydsl;

import java.util.List;
import site.bannabe.server.domain.rentals.controller.response.RentalStationDetailResponse.RentalItemResponse;

public interface CustomRentalStationRepository {

  List<RentalItemResponse> findRentalStationDetailBy(Long stationId);

}