package site.bannabe.server.domain.rentals.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.bannabe.server.domain.rentals.controller.response.ReturnItemDetailResponse;
import site.bannabe.server.domain.rentals.entity.RentalHistory;
import site.bannabe.server.domain.rentals.entity.RentalItems;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.rentals.entity.RentalStatus;
import site.bannabe.server.domain.rentals.repository.RentalStationRepository;
import site.bannabe.server.domain.users.entity.Users;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;
import site.bannabe.server.global.jwt.UserTokenService;
import site.bannabe.server.global.type.UserTokens;

@Service
@RequiredArgsConstructor
public class ReturnService {

  private final RentalStationRepository rentalStationRepository;
  private final RentalHistoryService rentalHistoryService;
  private final StockLockService stockLockService;
  private final UserTokenService userTokenService;

  @Transactional
  public ReturnItemDetailResponse getReturnItemInfo(String rentalItemToken, Long currentStationId) {
    RentalHistory rentalHistory = rentalHistoryService.findRentalHistoryByRentalItemToken(rentalItemToken);
    RentalItems rentalItem = rentalHistory.getRentalItem();
    if (!rentalItem.isRented()) {
      throw new BannabeServiceException(ErrorCode.RENTAL_ITEM_NOT_RENTED);
    }
    rentalHistory.validateOverdue(LocalDateTime.now());
    RentalStations currentStation = rentalStationRepository.findById(currentStationId)
                                                           .orElseThrow(() ->
                                                               new BannabeServiceException(ErrorCode.RENTAL_STATION_NOT_FOUND));
    sendOverduePushAlert(rentalHistory);
    return ReturnItemDetailResponse.from(rentalHistory, currentStation);
  }

  @Transactional
  public void returnRentalItem(String rentalItemToken, Long returnStationId) {
    RentalHistory rentalHistory = rentalHistoryService.findRentalHistoryByRentalItemToken(rentalItemToken);
    if (rentalHistory.getStatus().equals(RentalStatus.OVERDUE)) {
      throw new BannabeServiceException(ErrorCode.RENTAL_ITEM_OVERDUE);
    }
    RentalStations currentStation = rentalStationRepository.findById(returnStationId)
                                                           .orElseThrow(() ->
                                                               new BannabeServiceException(ErrorCode.RENTAL_STATION_NOT_FOUND));

    RentalItems rentalItem = rentalHistory.getRentalItem();
    rentalHistory.updateOnReturn(currentStation, LocalDateTime.now());
    rentalItem.updateOnReturn(currentStation);
    stockLockService.increaseStock(rentalItem);
  }

  private void sendOverduePushAlert(RentalHistory rentalHistory) {
    if (!rentalHistory.getStatus().equals(RentalStatus.OVERDUE)) {
      return;
    }
    Users user = rentalHistory.getUser();
    String entityToken = user.getToken();
    List<UserTokens> userTokens = userTokenService.findAllUserTokens(entityToken);

    userTokens.stream().map(UserTokens::getDeviceToken).forEach(deviceToken -> {
      // 여기서 FCM에게 푸시알림 보내는 로직 수행하도록 처리
    });
  }

}