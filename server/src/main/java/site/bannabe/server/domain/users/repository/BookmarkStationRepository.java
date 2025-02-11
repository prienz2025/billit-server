package site.bannabe.server.domain.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import site.bannabe.server.domain.users.entity.BookmarkStations;
import site.bannabe.server.domain.users.repository.querydsl.CustomBookmarkStationRepository;

public interface BookmarkStationRepository extends JpaRepository<BookmarkStations, Long>, CustomBookmarkStationRepository {

  @Modifying
  @Query("delete from BookmarkStations bs where bs.id = :bookmarkId")
  void deleteBookmarkById(@NonNull @Param("bookmarkId") Long id);

}