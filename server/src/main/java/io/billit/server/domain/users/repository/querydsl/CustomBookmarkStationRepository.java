package io.billit.server.domain.users.repository.querydsl;

import java.util.List;
import io.billit.server.domain.rentals.entity.RentalStations;
import io.billit.server.domain.users.controller.response.UserBookmarkStationsResponse.BookmarkStationResponse;
import io.billit.server.domain.users.entity.Users;

public interface CustomBookmarkStationRepository {

  List<BookmarkStationResponse> findBookmarkStationsBy(String entityToken);

  boolean existsByTokenAndId(String entityToken, Long bookmarkId);

  boolean existsByUserAndStation(Users user, RentalStations station);

}