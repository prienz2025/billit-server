package io.billit.server.domain.rentals.repository.querydsl;

import static io.billit.server.domain.rentals.entity.QRentalItems.rentalItems;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import io.billit.server.domain.rentals.entity.RentalItems;
import io.billit.server.global.exceptions.BannabeServiceException;
import io.billit.server.global.exceptions.ErrorCode;

@Repository
@RequiredArgsConstructor
public class CustomRentalItemRepositoryImpl implements CustomRentalItemRepository {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public RentalItems findByToken(String token) {
    RentalItems rentalItem = jpaQueryFactory.selectFrom(rentalItems)
                                            .join(rentalItems.rentalItemType).fetchJoin()
                                            .join(rentalItems.currentStation).fetchJoin()
                                            .where(rentalItems.token.eq(token))
                                            .fetchOne();
    return Optional.ofNullable(rentalItem).orElseThrow(() -> new BannabeServiceException(ErrorCode.RENTAL_ITEM_NOT_FOUND));
  }

  @Override
  public Integer findRentalItemPrice(String token) {
    Integer price = jpaQueryFactory.select(rentalItems.rentalItemType.price)
                                   .from(rentalItems)
                                   .join(rentalItems.rentalItemType)
                                   .where(rentalItems.token.eq(token))
                                   .fetchOne();
    return Optional.ofNullable(price).orElseThrow(() -> new BannabeServiceException(ErrorCode.RENTAL_ITEM_NOT_FOUND));
  }

}