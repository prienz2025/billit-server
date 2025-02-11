package site.bannabe.server.domain.rentals.repository.querydsl;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import site.bannabe.server.domain.rentals.controller.response.RentalSuccessSimpleResponse;
import site.bannabe.server.domain.rentals.entity.RentalHistory;

public interface CustomRentalHistoryRepository {

  List<RentalHistory> findActiveRentalsBy(String entityToken);

  Page<RentalHistory> findAllRentalsBy(String entityToken, Pageable pageable);

  RentalSuccessSimpleResponse findRentalHistoryInfoBy(String token);

  RentalHistory findByItemToken(String rentalItemToken);

}