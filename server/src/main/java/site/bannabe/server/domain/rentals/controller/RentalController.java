package site.bannabe.server.domain.rentals.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.bannabe.server.domain.rentals.controller.response.RentalItemDetailResponse;
import site.bannabe.server.domain.rentals.controller.response.RentalSuccessSimpleResponse;
import site.bannabe.server.domain.rentals.service.RentalService;

@RestController
@RequestMapping("/v1/rentals")
@RequiredArgsConstructor
public class RentalController {

  private final RentalService rentalService;

  @GetMapping("/{rentalItemToken}")
  public RentalItemDetailResponse getRentalItemInfo(@PathVariable String rentalItemToken) {
    return rentalService.getRentalItemInfo(rentalItemToken);
  }

  @GetMapping("/success/{rentalHistoryToken}")
  public RentalSuccessSimpleResponse getRentalHistoryInfo(@PathVariable("rentalHistoryToken") String rentalHistoryToken) {
    return rentalService.getRentalHistoryInfo(rentalHistoryToken);
  }

}