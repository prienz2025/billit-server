package site.bannabe.server.domain.users.controller.response;

import java.util.List;

public record UserBookmarkStationsResponse(
    List<BookmarkStationResponse> bookmarks
) {

  public record BookmarkStationResponse(
      String name,
      Long stationId,
      Long bookmarkId
  ) {

  }
}