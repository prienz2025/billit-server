package site.bannabe.server.domain.rentals.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.bannabe.server.domain.rentals.controller.request.ReturnStationRequest;
import site.bannabe.server.domain.rentals.controller.response.ReturnItemDetailResponse;
import site.bannabe.server.domain.rentals.service.ReturnService;

@RestController
@RequestMapping("/v1/returns")
@RequiredArgsConstructor
public class ReturnController {

  private final ReturnService returnService;

  //  @PreAuthorize("hasRole('STATION')")
  @GetMapping("/{rentalItemToken}")
  public ReturnItemDetailResponse getReturnItemInfo(
      @PathVariable("rentalItemToken") String rentalItemToken,
      @RequestParam("currentStationId") Long currentStationId
  ) {
    return returnService.getReturnItemInfo(rentalItemToken, currentStationId);
  }

  //  @PreAuthorize("hasRole('STATION')")
  @PostMapping("/{rentalItemToken}")
  public void returnItem(
      @PathVariable("rentalItemToken") String rentalItemToken,
      @RequestBody ReturnStationRequest returnStationRequest
  ) {
    returnService.returnRentalItem(rentalItemToken, returnStationRequest.returnStationId());
  }

}