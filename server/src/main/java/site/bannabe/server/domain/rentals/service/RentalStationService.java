package site.bannabe.server.domain.rentals.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.bannabe.server.domain.rentals.controller.response.RentalItemTypeDetailResponse;
import site.bannabe.server.domain.rentals.controller.response.RentalStationDetailResponse;
import site.bannabe.server.domain.rentals.controller.response.RentalStationDetailResponse.RentalItemResponse;
import site.bannabe.server.domain.rentals.controller.response.RentalStationSimpleResponse;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.rentals.repository.RentalItemTypeRepository;
import site.bannabe.server.domain.rentals.repository.RentalStationRepository;
import site.bannabe.server.domain.users.entity.BookmarkStations;
import site.bannabe.server.domain.users.entity.Users;
import site.bannabe.server.domain.users.repository.BookmarkStationRepository;
import site.bannabe.server.domain.users.repository.UserRepository;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;

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