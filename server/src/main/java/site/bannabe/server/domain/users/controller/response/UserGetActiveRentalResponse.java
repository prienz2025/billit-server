package site.bannabe.server.domain.users.controller.response;

import java.time.LocalDateTime;
import java.util.List;
import site.bannabe.server.domain.rentals.entity.RentalHistory;

public record UserGetActiveRentalResponse(
    List<RentalHistoryResponse> rentals
) {

  public record RentalHistoryResponse(
      String name,
      String status,
      Integer rentalTimeHour,
      LocalDateTime startTime,
      LocalDateTime expectedReturnTime,
      String token
  ) {

    public static RentalHistoryResponse of(RentalHistory rentalHistory) {
      return new RentalHistoryResponse(
          rentalHistory.getRentalItem().getRentalItemType().getName(),
          rentalHistory.getStatus().getDescription(),
          rentalHistory.getRentalTimeHour(),
          rentalHistory.getStartTime(),
          rentalHistory.getExpectedReturnTime(),
          rentalHistory.getToken()
      );
    }

  }

}