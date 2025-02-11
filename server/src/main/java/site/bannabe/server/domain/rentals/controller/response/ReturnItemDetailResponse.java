package site.bannabe.server.domain.rentals.controller.response;

import java.time.LocalDateTime;
import site.bannabe.server.domain.rentals.entity.RentalHistory;
import site.bannabe.server.domain.rentals.entity.RentalStations;

public record ReturnItemDetailResponse(
    String rentalItemName,
    RentalHistoryResponse rentalHistory,
    RentalStationResponse rentalStation
) {

  public static ReturnItemDetailResponse from(RentalHistory rentalHistory, RentalStations currentStation) {
    return new ReturnItemDetailResponse(
        rentalHistory.getRentalItem().getRentalItemType().getName(),
        RentalHistoryResponse.from(rentalHistory),
        RentalStationResponse.from(rentalHistory.getRentalStation(), currentStation)
    );
  }

  public record RentalHistoryResponse(
      String status,
      Integer rentalTime,
      LocalDateTime startTime,
      LocalDateTime expectedReturnTime
  ) {

    private static RentalHistoryResponse from(RentalHistory rentalHistory) {
      return new RentalHistoryResponse(
          rentalHistory.getStatus().name(),
          rentalHistory.getRentalTimeHour(),
          rentalHistory.getStartTime(),
          rentalHistory.getExpectedReturnTime()
      );
    }
  }

  public record RentalStationResponse(
      String rentalStationName,
      String currentStationName
  ) {

    private static RentalStationResponse from(RentalStations rentalStation, RentalStations currentStation) {
      return new RentalStationResponse(
          rentalStation.getName(),
          currentStation.getName()
      );
    }
  }
}