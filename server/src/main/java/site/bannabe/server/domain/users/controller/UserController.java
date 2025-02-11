package site.bannabe.server.domain.users.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.bannabe.server.domain.users.controller.request.UserChangeNicknameRequest;
import site.bannabe.server.domain.users.controller.request.UserChangePasswordRequest;
import site.bannabe.server.domain.users.controller.request.UserChangeProfileImageRequest;
import site.bannabe.server.domain.users.controller.response.S3PreSignedUrlResponse;
import site.bannabe.server.domain.users.controller.response.UserBookmarkStationsResponse;
import site.bannabe.server.domain.users.controller.response.UserGetActiveRentalResponse;
import site.bannabe.server.domain.users.controller.response.UserGetActiveRentalResponse.RentalHistoryResponse;
import site.bannabe.server.domain.users.controller.response.UserGetSimpleResponse;
import site.bannabe.server.domain.users.service.UserService;
import site.bannabe.server.global.security.auth.PrincipalDetails;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  public UserGetSimpleResponse getUserInfo(@AuthenticationPrincipal PrincipalDetails principalDetails) {
    String entityToken = principalDetails.getEntityToken();
    return userService.getUserInfo(entityToken);
  }

  @PutMapping("/me/password")
  public void changePassword(@RequestBody @Valid UserChangePasswordRequest changePasswordRequest,
      @AuthenticationPrincipal PrincipalDetails principalDetails) {
    String entityToken = principalDetails.getEntityToken();
    userService.changePassword(entityToken, changePasswordRequest);
  }

  @PatchMapping("/me/profile-image")
  public void changeProfileImage(@RequestBody UserChangeProfileImageRequest changeProfileImageRequest,
      @AuthenticationPrincipal PrincipalDetails principalDetails) {
    String entityToken = principalDetails.getEntityToken();
    userService.changeProfileImage(entityToken, changeProfileImageRequest);
  }

  @PatchMapping("/me/profile-image/default")
  public void changeProfileImageToDefault(@AuthenticationPrincipal PrincipalDetails principalDetails) {
    String entityToken = principalDetails.getEntityToken();
    userService.changeProfileImageToDefault(entityToken);
  }

  @PatchMapping("/me/nickname")
  public void changeNickname(@RequestBody @Valid UserChangeNicknameRequest changeNicknameRequest,
      @AuthenticationPrincipal PrincipalDetails principalDetails) {
    String entityToken = principalDetails.getEntityToken();
    userService.changeNickname(entityToken, changeNicknameRequest);
  }

  @GetMapping("/me/profile-image/pre-signed")
  public S3PreSignedUrlResponse getPreSignedUrl(@RequestParam(name = "extension") String extension) {
    return userService.getPreSignedUrl(extension);
  }

  @GetMapping("/me/rentals/active")
  public UserGetActiveRentalResponse getActiveRentalHistory(@AuthenticationPrincipal PrincipalDetails principalDetails) {
    String entityToken = principalDetails.getEntityToken();
    return userService.getActiveRentalHistory(entityToken);
  }

  @GetMapping("/me/rentals")
  public Page<RentalHistoryResponse> getRentalHistory(@AuthenticationPrincipal PrincipalDetails principalDetails,
      @PageableDefault(sort = "startTime", direction = Direction.DESC) Pageable pageable) {
    String entityToken = principalDetails.getEntityToken();
    return userService.getRentalHistory(entityToken, pageable);
  }

  @GetMapping("/me/stations/bookmark")
  public UserBookmarkStationsResponse getBookmarkStations(@AuthenticationPrincipal PrincipalDetails principalDetails) {
    String entityToken = principalDetails.getEntityToken();
    return userService.getBookmarkStations(entityToken);
  }

  @DeleteMapping("/me/stations/bookmark/{bookmarkId}")
  public void deleteBookmarkStation(@PathVariable("bookmarkId") Long bookmarkId,
      @AuthenticationPrincipal PrincipalDetails principalDetails) {
    String entityToken = principalDetails.getEntityToken();
    userService.removeBookmarkStation(entityToken, bookmarkId);
  }

}