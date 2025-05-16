package io.billit.server.domain.rentals.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.billit.server.domain.rentals.controller.response.RentalItemTypeDetailResponse;
import io.billit.server.domain.rentals.controller.response.RentalStationDetailResponse;
import io.billit.server.domain.rentals.controller.response.RentalStationDetailResponse.RentalItemResponse;
import io.billit.server.domain.rentals.controller.response.RentalStationSimpleResponse;
import io.billit.server.domain.rentals.entity.RentalStations;
import io.billit.server.domain.rentals.repository.RentalItemTypeRepository;
import io.billit.server.domain.rentals.repository.RentalStationRepository;
import io.billit.server.domain.users.entity.BookmarkStations;
import io.billit.server.domain.users.entity.Users;
import io.billit.server.domain.users.repository.BookmarkStationRepository;
import io.billit.server.domain.users.repository.UserRepository;
import io.billit.server.global.exceptions.BannabeServiceException;
import io.billit.server.global.exceptions.ErrorCode;

@Service
@RequiredArgsConstructor
public class RentalStationService {

  private final RentalStationRepository rentalStationRepository;
  private final RentalItemTypeRepository rentalItemTypeRepository;
  private final UserRepository userRepository;
  private final BookmarkStationRepository bookmarkStationRepository;

  @Transactional(readOnly = true)
  public RentalStationSimpleResponse getAllRentalStations() {
    List<RentalStations> rentalStations = rentalStationRepository.findAll();
    return RentalStationSimpleResponse.create(rentalStations);
  }

  @Transactional(readOnly = true)
  public RentalStationDetailResponse getRentalStationDetail(Long stationId) {
    List<RentalItemResponse> rentalItemResponses = rentalStationRepository.findRentalStationDetailBy(stationId);
    return new RentalStationDetailResponse(rentalItemResponses);
  }

  @Transactional(readOnly = true)
  public RentalItemTypeDetailResponse getRentalItemTypeDetail(Long stationId, Long itemTypeId) {
    return rentalItemTypeRepository.findRentalItemDetailBy(stationId, itemTypeId);
  }

  @Transactional
  public void bookmarkRentalStation(Long stationId, String entityToken) {
    Users user = userRepository.findByToken(entityToken);
    RentalStations rentalStation = rentalStationRepository.findById(stationId)
                                                          .orElseThrow(() ->
                                                              new BannabeServiceException(ErrorCode.RENTAL_STATION_NOT_FOUND));

    boolean isAlreadyBookmarked = bookmarkStationRepository.existsByUserAndStation(user, rentalStation);
    if (isAlreadyBookmarked) {
      throw new BannabeServiceException(ErrorCode.ALREADY_BOOKMARKED);
    }

    BookmarkStations bookmarkStations = new BookmarkStations(user, rentalStation);
    bookmarkStationRepository.save(bookmarkStations);
  }

}