package io.billit.server.domain.rentals.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.billit.server.domain.rentals.controller.response.RentalItemDetailResponse;
import io.billit.server.domain.rentals.controller.response.RentalSuccessSimpleResponse;
import io.billit.server.domain.rentals.entity.RentalItems;
import io.billit.server.domain.rentals.repository.RentalHistoryRepository;
import io.billit.server.domain.rentals.repository.RentalItemRepository;
import io.billit.server.global.exceptions.BannabeServiceException;
import io.billit.server.global.exceptions.ErrorCode;

@Service
@RequiredArgsConstructor
public class RentalService {

  private final RentalItemRepository rentalItemRepository;
  private final RentalHistoryRepository rentalHistoryRepository;

  @Transactional(readOnly = true)
  public RentalItemDetailResponse getRentalItemInfo(String rentalItemToken) {
    RentalItems rentalItem = rentalItemRepository.findByToken(rentalItemToken);
    if (rentalItem.isRented()) {
      throw new BannabeServiceException(ErrorCode.RENTAL_ITEM_ALREADY_RENTED);
    }
    return RentalItemDetailResponse.create(rentalItem);
  }

  @Transactional(readOnly = true)
  public RentalSuccessSimpleResponse getRentalHistoryInfo(String rentalHistoryToken) {
    return rentalHistoryRepository.findRentalHistoryInfoBy(rentalHistoryToken);
  }

}