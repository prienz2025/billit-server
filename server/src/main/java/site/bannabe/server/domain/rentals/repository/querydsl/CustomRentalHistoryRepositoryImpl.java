package site.bannabe.server.domain.rentals.repository.querydsl;

import static site.bannabe.server.domain.payments.entity.QRentalPayments.rentalPayments;
import static site.bannabe.server.domain.rentals.entity.QRentalHistory.rentalHistory;
import static site.bannabe.server.domain.rentals.entity.QRentalItemTypes.rentalItemTypes;
import static site.bannabe.server.domain.rentals.entity.QRentalItems.rentalItems;
import static site.bannabe.server.domain.rentals.entity.QRentalStations.rentalStations;
import static site.bannabe.server.domain.rentals.entity.RentalStatus.OVERDUE;
import static site.bannabe.server.domain.rentals.entity.RentalStatus.RENTAL;
import static site.bannabe.server.domain.users.entity.QUsers.users;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import site.bannabe.server.domain.rentals.controller.response.RentalSuccessSimpleResponse;
import site.bannabe.server.domain.rentals.entity.RentalHistory;
import site.bannabe.server.domain.rentals.entity.RentalStatus;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;

@Repository
@RequiredArgsConstructor
public class CustomRentalHistoryRepositoryImpl implements CustomRentalHistoryRepository {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public List<RentalHistory> findActiveRentalsBy(String entityToken) {
    return jpaQueryFactory.selectFrom(rentalHistory)
                          .join(rentalHistory.user, users)
                          .leftJoin(rentalHistory.rentalItem, rentalItems).fetchJoin()
                          .leftJoin(rentalItems.rentalItemType, rentalItemTypes).fetchJoin()
                          .where(
                              users.token.eq(entityToken)
                                         .and(rentalHistory.status.in(RENTAL, OVERDUE))
                          )
                          .orderBy(rentalHistory.startTime.desc())
                          .fetch();
  }

  @Override
  public Page<RentalHistory> findAllRentalsBy(String entityToken, Pageable pageable) {
    List<RentalHistory> rentalHistories = jpaQueryFactory.selectFrom(rentalHistory)
                                                         .leftJoin(rentalHistory.rentalItem, rentalItems).fetchJoin()
                                                         .leftJoin(rentalItems.rentalItemType, rentalItemTypes).fetchJoin()
                                                         .leftJoin(rentalHistory.user, users)
                                                         .where(users.token.eq(entityToken))
                                                         .orderBy(getOrderSpecifiers(pageable.getSort()))
                                                         .offset(pageable.getOffset())
                                                         .limit(pageable.getPageSize())
                                                         .fetch();
    JPAQuery<Long> queryCount = jpaQueryFactory.select(rentalHistory.count())
                                               .from(rentalHistory)
                                               .leftJoin(rentalHistory.user, users)
                                               .where(users.token.eq(entityToken));
    return PageableExecutionUtils.getPage(rentalHistories, pageable, queryCount::fetchOne);
  }

  @Override
  public RentalSuccessSimpleResponse findRentalHistoryInfoBy(String token) {
    RentalSuccessSimpleResponse result = jpaQueryFactory.select(Projections.constructor(
                                                            RentalSuccessSimpleResponse.class,
                                                            rentalPayments.totalAmount,
                                                            rentalItemTypes.name,
                                                            rentalHistory.rentalTimeHour,
                                                            rentalStations.name
                                                        )).from(rentalPayments)
                                                        .leftJoin(rentalPayments.rentalHistory, rentalHistory)
                                                        .leftJoin(rentalHistory.rentalStation, rentalStations)
                                                        .leftJoin(rentalHistory.rentalItem, rentalItems)
                                                        .leftJoin(rentalItems.rentalItemType, rentalItemTypes)
                                                        .where(rentalHistory.token.eq(token))
                                                        .fetchOne();
    return Optional.ofNullable(result).orElseThrow(() -> new BannabeServiceException(ErrorCode.ORDER_INFO_NOT_FOUND));
  }

  @Override
  public RentalHistory findByItemToken(String rentalItemToken) {
    RentalHistory result = jpaQueryFactory.selectFrom(rentalHistory)
                                          .join(rentalHistory.rentalItem, rentalItems).fetchJoin()
                                          .join(rentalItems.rentalItemType, rentalItemTypes).fetchJoin()
                                          .where(rentalItems.token.eq(rentalItemToken)
                                                                  .and(rentalHistory.status.ne(RentalStatus.RETURNED)))
                                          .orderBy(rentalHistory.startTime.desc())
                                          .fetchFirst();
    return Optional.ofNullable(result).orElseThrow(() -> new BannabeServiceException(ErrorCode.RENTAL_HISTORY_NOT_FOUND));
  }

  @SuppressWarnings("unchecked")
  private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
    return sort.stream().map(order -> {
      Order direction = order.isAscending() ? Order.ASC : Order.DESC;
      String property = order.getProperty();
      return new OrderSpecifier(direction, Expressions.path(RentalHistory.class, rentalHistory, property));
    }).toArray(OrderSpecifier[]::new);
  }

}