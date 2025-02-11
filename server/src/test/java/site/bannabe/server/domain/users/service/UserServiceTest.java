package site.bannabe.server.domain.users.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import site.bannabe.server.domain.rentals.entity.RentalHistory;
import site.bannabe.server.domain.rentals.entity.RentalItemTypes;
import site.bannabe.server.domain.rentals.entity.RentalItems;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.rentals.entity.RentalStatus;
import site.bannabe.server.domain.rentals.repository.RentalHistoryRepository;
import site.bannabe.server.domain.users.controller.request.UserChangeNicknameRequest;
import site.bannabe.server.domain.users.controller.request.UserChangePasswordRequest;
import site.bannabe.server.domain.users.controller.request.UserChangeProfileImageRequest;
import site.bannabe.server.domain.users.controller.response.UserBookmarkStationsResponse;
import site.bannabe.server.domain.users.controller.response.UserBookmarkStationsResponse.BookmarkStationResponse;
import site.bannabe.server.domain.users.controller.response.UserGetActiveRentalResponse;
import site.bannabe.server.domain.users.controller.response.UserGetActiveRentalResponse.RentalHistoryResponse;
import site.bannabe.server.domain.users.controller.response.UserGetSimpleResponse;
import site.bannabe.server.domain.users.entity.Users;
import site.bannabe.server.domain.users.repository.BookmarkStationRepository;
import site.bannabe.server.domain.users.repository.UserRepository;
import site.bannabe.server.global.aws.S3Service;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  private static final String EMAIL = "test@test.com";
  private final String entityToken = "entityToken";
  @Mock
  private UserRepository userRepository;
  @Mock
  private RentalHistoryRepository rentalHistoryRepository;
  @Mock
  private BookmarkStationRepository bookmarkStationRepository;
  @Mock
  private S3Service s3Service;
  @Mock
  private PasswordService passwordService;
  @InjectMocks
  private UserService userService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(userService, "defaultProfileImage", "defaultProfileImage.png");
  }

  @Test
  @DisplayName("회원정보 조회 성공")
  void getUserInfo() {
    //given
    String entityToken = "entityToken";
    Users user = Users.builder().nickname("테스트 닉네임").profileImage("프로필 이미지").email("이메일").build();

    given(userRepository.findByToken(entityToken)).willReturn(user);

    //when
    UserGetSimpleResponse result = userService.getUserInfo(entityToken);

    //then
    assertThat(result).isNotNull()
                      .extracting(UserGetSimpleResponse::email,
                          UserGetSimpleResponse::nickname,
                          UserGetSimpleResponse::profileImage)
                      .containsExactly(
                          user.getEmail(),
                          user.getNickname(),
                          user.getProfileImage()
                      );
  }

  @Test
  @DisplayName("비밀번호 변경 성공")
  void changePassword() {
    //given
    String currentPassword = "currentPassword";
    String currentEncodedPassword = "currentEncodedPassword";
    String newPassword = "newPassword";
    String newPasswordConfirm = "newPassword";
    String encodedPassword = "encodedPassword";
    UserChangePasswordRequest request = new UserChangePasswordRequest(currentPassword, newPassword, newPasswordConfirm);
    Users user = Users.builder().email(EMAIL).password(currentEncodedPassword).build();

    given(userRepository.findByToken(entityToken)).willReturn(user);
    given(passwordService.encodePassword(newPassword)).willReturn(encodedPassword);

    //when
    userService.changePassword(entityToken, request);

    //then
    assertThat(user.getPassword()).isEqualTo(encodedPassword);
    verify(passwordService).validateNewPassword(newPassword, newPasswordConfirm);
    verify(passwordService).validateCurrentPassword(currentPassword, currentEncodedPassword);
    verify(passwordService).validateReusedPassword(newPassword, currentEncodedPassword);
  }

  @Test
  @DisplayName("비밀번호 변경 중 새 비밀번호 불일치 시 예외 발생")
  void invalidNewPassword() {
    //given
    String currentPassword = "currentPassword";
    String newPassword = "newPassword";
    String newPasswordConfirm = "newPasswordConfirm";
    UserChangePasswordRequest request = new UserChangePasswordRequest(currentPassword, newPassword, newPasswordConfirm);
    willThrow(new BannabeServiceException(ErrorCode.NEW_PASSWORD_MISMATCH)).given(passwordService)
                                                                           .validateNewPassword(newPassword, newPasswordConfirm);

    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> userService.changePassword(entityToken, request))
        .withMessage(ErrorCode.NEW_PASSWORD_MISMATCH.getMessage());

    verify(passwordService, never()).validateCurrentPassword(eq(currentPassword), anyString());
    verify(passwordService, never()).validateReusedPassword(eq(newPassword), anyString());
    verify(passwordService, never()).encodePassword(newPassword);
  }

  @Test
  @DisplayName("비밀번호 변경 중 현재 비밀번호 불일치 시 예외 발생")
  void changePasswordInvalidCurrentPassword() {
    //given
    String currentPassword = "currentPassword";
    String currentEncodedPassword = "currentEncodedPassword";
    String newPassword = "newPassword";
    String newPasswordConfirm = "newPasswordConfirm";
    UserChangePasswordRequest request = new UserChangePasswordRequest(currentPassword, newPassword, newPasswordConfirm);
    Users user = Users.builder().email(EMAIL).password(currentEncodedPassword).build();

    given(userRepository.findByToken(entityToken)).willReturn(user);
    willThrow(new BannabeServiceException(ErrorCode.PASSWORD_MISMATCH)).given(passwordService)
                                                                       .validateCurrentPassword(currentPassword,
                                                                           user.getPassword());
    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> userService.changePassword(entityToken, request))
        .withMessage(ErrorCode.PASSWORD_MISMATCH.getMessage());

    verify(passwordService).validateNewPassword(newPassword, newPasswordConfirm);
    verify(passwordService).validateCurrentPassword(eq(currentPassword), anyString());
    verify(passwordService, never()).validateReusedPassword(eq(newPassword), anyString());
    verify(passwordService, never()).encodePassword(newPassword);
  }

  @Test
  @DisplayName("비밀번호 변경 중 새 비밀번호가 현재 비밀번호와 일치할 시 예외 발생")
  void changePasswordReusedPassword() {
    //given
    String currentPassword = "currentPassword";
    String currentEncodedPassword = "currentEncodedPassword";
    String newPassword = "newPassword";
    String newPasswordConfirm = "newPasswordConfirm";
    UserChangePasswordRequest request = new UserChangePasswordRequest(currentPassword, newPassword, newPasswordConfirm);
    Users user = mock(Users.class);

    given(userRepository.findByToken(entityToken)).willReturn(user);
    given(user.getPassword()).willReturn(currentEncodedPassword);
    willThrow(new BannabeServiceException(ErrorCode.DUPLICATE_PASSWORD)).given(passwordService)
                                                                        .validateReusedPassword(newPassword,
                                                                            currentEncodedPassword);
    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> userService.changePassword(entityToken, request))
        .withMessage(ErrorCode.DUPLICATE_PASSWORD.getMessage());

    verify(passwordService).validateNewPassword(newPassword, newPasswordConfirm);
    verify(passwordService).validateCurrentPassword(eq(currentPassword), anyString());
    verify(passwordService).validateReusedPassword(eq(newPassword), eq(currentEncodedPassword));
    verify(passwordService, never()).encodePassword(newPassword);
  }

  @Test
  @DisplayName("닉네임 변경 성공")
  void changeNickname() {
    //given
    String newNickname = "nickname";
    String currentNickname = "currentNickname";
    Users user = Users.builder().email(EMAIL).nickname(currentNickname).build();

    given(userRepository.existsByNickname(newNickname)).willReturn(Boolean.FALSE);
    given(userRepository.findByToken(entityToken)).willReturn(user);

    //when
    userService.changeNickname(entityToken, new UserChangeNicknameRequest(newNickname));

    //then
    assertThat(user.getNickname()).isEqualTo(newNickname);

    verify(userRepository).existsByNickname(newNickname);
    verify(userRepository).findByToken(entityToken);
  }

  @Test
  @DisplayName("닉네임 중복시 예외 발생")
  void duplicateNickname() {
    //given
    String newNickname = "nickname";

    given(userRepository.existsByNickname(newNickname)).willReturn(Boolean.TRUE);

    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> userService.changeNickname(entityToken, new UserChangeNicknameRequest(newNickname)))
        .withMessage(ErrorCode.DUPLICATE_NICKNAME.getMessage());

    verify(userRepository, never()).findByToken(entityToken);
  }

  @Test
  @DisplayName("프로필 이미지 변경 성공 테스트")
  void changeProfileImage() {
    //given
    String newImageUrl = "newImageUrl";
    String currentProfileImage = "currentProfileImage";
    Users user = Users.builder().email(EMAIL).profileImage(currentProfileImage).build();
    given(userRepository.findByToken(entityToken)).willReturn(user);

    //when
    userService.changeProfileImage(entityToken, new UserChangeProfileImageRequest(newImageUrl));

    //then
    assertThat(user.getProfileImage()).isEqualTo(newImageUrl);
    verify(s3Service).removeProfileImage(currentProfileImage);
  }

  @Test
  @DisplayName("대여현황 중 expectedReturnTime이 현재 시간보다 이전이면 Overdue로 상태 변경")
  void validateOverdueFromActiveRentalHistory() {
    //given
    int rentalTimeHour = 1;
    LocalDateTime startTime = LocalDateTime.now().minusDays(1);
    String token = "token";

    RentalItemTypes rentalItemType = RentalItemTypes.builder().name("충전기").build();
    RentalItems rentalItem = RentalItems.builder().rentalItemType(rentalItemType).build();
    RentalHistory rentalHistory = RentalHistory.builder().rentalItem(rentalItem)
                                               .rentalTimeHour(rentalTimeHour)
                                               .startTime(startTime)
                                               .expectedReturnTime(startTime.plusHours(rentalTimeHour))
                                               .token(token)
                                               .status(RentalStatus.RENTAL)
                                               .build();
    List<RentalHistory> rentalHistories = List.of(rentalHistory);
    given(rentalHistoryRepository.findActiveRentalsBy(entityToken)).willReturn(rentalHistories);

    //when
    UserGetActiveRentalResponse result = userService.getActiveRentalHistory(entityToken);

    //then
    assertThat(result).isNotNull();
    assertThat(result.rentals().get(0)).isNotNull()
                                       .extracting(RentalHistoryResponse::status)
                                       .isEqualTo(RentalStatus.OVERDUE.getDescription());
  }

  @Test
  @DisplayName("현재 대여중인 내역이 없다면 빈 List 반환")
  void isEmptyActiveRentalHistory() {
    //given
    given(rentalHistoryRepository.findActiveRentalsBy(entityToken)).willReturn(List.of());

    //when
    UserGetActiveRentalResponse result = userService.getActiveRentalHistory(entityToken);

    //then
    assertThat(result.rentals()).isEmpty();
  }

  @Test
  @DisplayName("대여내역 중 expectedReturnTime이 현재 시간보다 이전이면 Overdue로 상태 변경")
  void validateOverdueFromAllRentalHistory() {
    //given
    PageRequest pageRequest = PageRequest.of(0, 10);
    int rentalTimeHour = 1;
    LocalDateTime startTime = LocalDateTime.now().minusHours(2);
    RentalHistory rentalHistory = RentalHistory.builder()
                                               .status(RentalStatus.RENTAL)
                                               .rentalItem(
                                                   RentalItems.builder().rentalItemType(
                                                       RentalItemTypes.builder().name("충전기").build()).build()
                                               )
                                               .rentalTimeHour(rentalTimeHour)
                                               .startTime(startTime)
                                               .expectedReturnTime(startTime.plusHours(rentalTimeHour))
                                               .token("token")
                                               .build();
    List<RentalHistory> rentalHistories = List.of(rentalHistory);
    PageImpl<RentalHistory> rentalHistoryPage = new PageImpl<>(rentalHistories);
    given(rentalHistoryRepository.findAllRentalsBy(entityToken, pageRequest)).willReturn(rentalHistoryPage);

    //when
    Page<RentalHistoryResponse> result = userService.getRentalHistory(entityToken, pageRequest);

    //then
    assertThat(result).isNotEmpty();
    assertThat(result.getContent().get(0)).isNotNull()
                                          .extracting(RentalHistoryResponse::status)
                                          .isEqualTo(RentalStatus.OVERDUE.getDescription());
  }

  @Test
  @DisplayName("대여내역 미 존재시 빈 리스트 반환")
  void isEmptyRentalHistory() {
    //given
    PageRequest pageRequest = PageRequest.of(0, 10);
    given(rentalHistoryRepository.findAllRentalsBy(entityToken, pageRequest)).willReturn(new PageImpl<>(List.of()));

    //when
    Page<RentalHistoryResponse> result = userService.getRentalHistory(entityToken, pageRequest);

    //then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("북마크 스테이션 조회")
  void getBookmarkStations() {
    //given
    List<RentalStations> stations = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      stations.add(RentalStations.builder().name("station" + i).build());
    }

    List<BookmarkStationResponse> bookmarks = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      bookmarks.add(new BookmarkStationResponse(stations.get(i).getName(), (long) i, (long) i));
    }

    given(bookmarkStationRepository.findBookmarkStationsBy(any())).willReturn(bookmarks);

    //when
    UserBookmarkStationsResponse result = userService.getBookmarkStations("test@test.com");

    //then
    assertThat(result).isNotNull();
    assertThat(result.bookmarks()).isNotEmpty().hasSize(bookmarks.size());
    for (int i = 0; i < 10; i++) {
      BookmarkStationResponse response = bookmarks.get(i);
      assertThat(response).isNotNull()
                          .extracting(BookmarkStationResponse::name)
                          .isEqualTo(stations.get(i).getName());
    }
  }

  @Test
  @DisplayName("북마크 스테이션 존재 시 북마크 삭제 메서드 호출")
  void removeBookmarkStation() {
    //given
    Long bookmarkId = 1L;

    given(bookmarkStationRepository.existsByTokenAndId(entityToken, bookmarkId)).willReturn(Boolean.TRUE);

    //when
    userService.removeBookmarkStation(entityToken, bookmarkId);

    //then
    verify(bookmarkStationRepository).deleteBookmarkById(bookmarkId);
  }

  @Test
  @DisplayName("북마크 스테이션 미 존재 시 북마크 예외 발생")
  void notExistBookmarkStation() {
    //given
    Long bookmarkId = 1L;

    given(bookmarkStationRepository.existsByTokenAndId(entityToken, bookmarkId)).willReturn(Boolean.FALSE);

    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> userService.removeBookmarkStation(entityToken, bookmarkId))
        .withMessage(ErrorCode.BOOKMARK_NOT_EXIST.getMessage());
  }

  @Test
  @DisplayName("프로필 이미지를 기본 이미지로 변경")
  void changeProfileImageToDefault() {
    //given
    String currentProfileImage = "currentProfileImage";
    Users user = Users.builder().email(EMAIL).profileImage(currentProfileImage).build();
    given(userRepository.findByToken(entityToken)).willReturn(user);

    //when
    userService.changeProfileImageToDefault(entityToken);

    //then
    assertThat(user.getProfileImage()).isEqualTo("defaultProfileImage.png");
    verify(s3Service).removeProfileImage(currentProfileImage);
  }

  @Test
  @DisplayName("프로필 이미지를 기본 이미지로 변경 시, 이미 기본 이미지라면 예외 발생")
  void alreadyDefaultProfileImage() {
    //given
    Users user = Users.builder().profileImage("defaultProfileImage.png").build();
    given(userRepository.findByToken(entityToken)).willReturn(user);

    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> userService.changeProfileImageToDefault(entityToken))
        .withMessage(ErrorCode.PROFILE_IMAGE_ALREADY_DEFAULT.getMessage());

    verify(s3Service, never()).removeProfileImage(anyString());
  }

}