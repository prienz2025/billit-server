package site.bannabe.server.domain.users.repository.querydsl;

import static site.bannabe.server.domain.rentals.entity.QRentalStations.rentalStations;
import static site.bannabe.server.domain.users.entity.QBookmarkStations.bookmarkStations;
import static site.bannabe.server.domain.users.entity.QUsers.users;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.users.controller.response.UserBookmarkStationsResponse.BookmarkStationResponse;
import site.bannabe.server.domain.users.entity.Users;

@Repository
@RequiredArgsConstructor
public class CustomBookmarkStationRepositoryImpl implements CustomBookmarkStationRepository {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public List<BookmarkStationResponse> findBookmarkStationsBy(String entityToken) {
    return jpaQueryFactory.select(
                              Projections.constructor(
                                  BookmarkStationResponse.class,
                                  rentalStations.name,
                                  rentalStations.id,
                                  bookmarkStations.id
                              ))
                          .from(bookmarkStations)
                          .join(bookmarkStations.user, users)
                          .join(bookmarkStations.rentalStation, rentalStations)
                          .where(users.token.eq(entityToken))
                          .orderBy(rentalStations.name.asc())
                          .fetch();
  }

  @Override
  public boolean existsByTokenAndId(String entityToken, Long bookmarkId) {
    Integer findBookmark = jpaQueryFactory.selectOne()
                                          .from(bookmarkStations)
                                          .join(bookmarkStations.user, users)
                                          .where(bookmarkStations.id.eq(bookmarkId)
                                                                    .and(users.token.eq(entityToken)))
                                          .fetchFirst();
    return findBookmark != null;
  }

  @Override
  public boolean existsByUserAndStation(Users user, RentalStations station) {
    Integer findBookmark = jpaQueryFactory.selectOne()
                                          .from(bookmarkStations)
                                          .where(bookmarkStations.user.eq(user)
                                                                      .and(bookmarkStations.rentalStation.eq(station)))
                                          .fetchFirst();
    return findBookmark != null;
  }

}