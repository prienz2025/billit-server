package site.bannabe.server.domain.rentals.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import site.bannabe.server.domain.rentals.entity.RentalHistory;
import site.bannabe.server.domain.rentals.entity.RentalItems;
import site.bannabe.server.domain.rentals.repository.RentalHistoryRepository;
import site.bannabe.server.domain.users.entity.Users;
import site.bannabe.server.global.type.OrderInfo;

@Service
@RequiredArgsConstructor
public class RentalHistoryService {

  private final RentalHistoryRepository rentalHistoryRepository;

  @Transactional(propagation = Propagation.REQUIRED)
  public RentalHistory findRentalHistoryByRentalItemToken(String rentalItemToken) {
    return rentalHistoryRepository.findByItemToken(rentalItemToken);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public RentalHistory saveRentalHistory(RentalItems rentalItem, Users user, OrderInfo orderInfo, LocalDateTime startTime) {
    RentalHistory rentalHistory = RentalHistory.create(rentalItem, user, orderInfo, startTime);
    return rentalHistoryRepository.save(rentalHistory);
  }

}