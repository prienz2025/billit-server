package io.billit.server.domain.rentals.repository.querydsl;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import io.billit.server.domain.rentals.controller.response.RentalSuccessSimpleResponse;
import io.billit.server.domain.rentals.entity.RentalHistory;

public interface CustomRentalHistoryRepository {

  List<RentalHistory> findActiveRentalsBy(String entityToken);

  Page<RentalHistory> findAllRentalsBy(String entityToken, Pageable pageable);

  RentalSuccessSimpleResponse findRentalHistoryInfoBy(String token);

  RentalHistory findByItemToken(String rentalItemToken);

}