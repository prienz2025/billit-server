package site.bannabe.server.domain.users.repository.querydsl;

import java.util.List;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.users.controller.response.UserBookmarkStationsResponse.BookmarkStationResponse;
import site.bannabe.server.domain.users.entity.Users;

public interface CustomBookmarkStationRepository {

  List<BookmarkStationResponse> findBookmarkStationsBy(String entityToken);

  boolean existsByTokenAndId(String entityToken, Long bookmarkId);

  boolean existsByUserAndStation(Users user, RentalStations station);

}