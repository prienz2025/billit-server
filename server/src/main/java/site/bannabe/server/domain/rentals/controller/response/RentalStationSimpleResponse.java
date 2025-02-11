package site.bannabe.server.domain.rentals.controller.response;

import java.math.BigDecimal;
import java.util.List;
import site.bannabe.server.domain.rentals.entity.RentalStations;

public record RentalStationSimpleResponse(
    List<StationResponse> stations
) {

  public static RentalStationSimpleResponse create(List<RentalStations> rentalStations) {
    List<StationResponse> stationResponses = rentalStations.stream().map(StationResponse::create).toList();
    return new RentalStationSimpleResponse(stationResponses);
  }

  public record StationResponse(
      String name,
      String address,
      BigDecimal latitude,
      BigDecimal longitude,
      String status,
      String businessTime,
      String grade,
      Long stationId
  ) {

    public static StationResponse create(RentalStations rentalStations) {
      return new StationResponse(
          rentalStations.getName(),
          rentalStations.getAddress(),
          rentalStations.getLatitude(),
          rentalStations.getLongitude(),
          rentalStations.getStatus().name(),
          rentalStations.getOpenTime() + " ~ " + rentalStations.getCloseTime(),
          rentalStations.getGrade().name(),
          rentalStations.getId()
      );
    }

  }

}