package site.bannabe.server.domain.users.controller;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import java.time.LocalDateTime;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.restassured.RestDocumentationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import site.bannabe.server.config.AbstractIntegrationTest;
import site.bannabe.server.domain.rentals.entity.RentalItemCategory;
import site.bannabe.server.domain.rentals.entity.RentalItemStatus;
import site.bannabe.server.domain.rentals.entity.RentalItemTypes;
import site.bannabe.server.domain.rentals.entity.RentalItems;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.rentals.entity.RentalStatus;
import site.bannabe.server.domain.users.controller.request.UserChangeNicknameRequest;
import site.bannabe.server.domain.users.controller.request.UserChangePasswordRequest;
import site.bannabe.server.domain.users.controller.request.UserChangeProfileImageRequest;
import site.bannabe.server.domain.users.controller.response.S3PreSignedUrlResponse;
import site.bannabe.server.domain.users.controller.response.UserGetActiveRentalResponse;
import site.bannabe.server.domain.users.controller.response.UserGetSimpleResponse;
import site.bannabe.server.domain.users.entity.BookmarkStations;
import site.bannabe.server.domain.users.entity.ProviderType;
import site.bannabe.server.domain.users.entity.Role;
import site.bannabe.server.domain.users.entity.Users;
import site.bannabe.server.domain.users.repository.UserRepository;
import site.bannabe.server.global.aws.S3Service;
import site.bannabe.server.global.jwt.GenerateToken;
import site.bannabe.server.global.jwt.JwtProvider;
import site.bannabe.server.util.EntityFixture;

class UserControllerTest extends AbstractIntegrationTest {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private EntityFixture entityFixture;
  @Autowired
  private BCryptPasswordEncoder passwordEncoder;
  @Autowired
  private JwtProvider jwtProvider;
  @MockitoBean
  private S3Service s3Service;

  private Users user;
  private String accessToken;

  @BeforeEach
  void setup() {
    user = Users.builder()
                .email("test@test.com")
                .nickname("TestUser")
                .profileImage("https://test.com/test.jpg")
                .password(passwordEncoder.encode("password"))
                .role(Role.USER)
                .providerType(ProviderType.LOCAL)
                .build();
    userRepository.saveAndFlush(user);
    GenerateToken generateToken = jwtProvider.generateToken(user.getToken(), user.getRole().getRoleKey());
    accessToken = generateToken.accessToken();
  }

  @AfterEach
  void tearDown() {
    entityFixture.clearAll();
  }

  @Test
  @DisplayName("회원 정보 조회(Me)")
  void getUserInfo() {
    //given when
    UserGetSimpleResponse result = given(this.spec)
        .filter(createGetUserInfoDocument()).log().all()
        .contentType(JSON)
        .header(AUTHORIZATION, "Bearer " + accessToken)
        .when()
        .get("/v1/users/me")
        .then().log().all()
        .assertThat()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .response().jsonPath().getObject("data", UserGetSimpleResponse.class);

    //then
    assertThat(result).isNotNull()
                      .extracting(
                          UserGetSimpleResponse::email,
                          UserGetSimpleResponse::nickname,
                          UserGetSimpleResponse::profileImage
                      ).containsExactly(
                          user.getEmail(),
                          user.getNickname(),
                          user.getProfileImage()
                      );
  }

  @Test
  @DisplayName("비밀번호 변경 성공시 200 응답")
  void changePassword() {
    //given
    UserChangePasswordRequest requestBody = new UserChangePasswordRequest("password", "newPassword", "newPassword");

    //when then
    given(this.spec)
        .filter(createChangePasswordDocument()).log().all()
        .contentType(JSON)
        .header(AUTHORIZATION, "Bearer " + accessToken)
        .body(requestBody)
        .when()
        .put("/v1/users/me/password")
        .then().log().all()
        .statusCode(HttpStatus.OK.value());
  }

  @Test
  @DisplayName("비밀번호 변경 실패시 409 응답")
  void mismatchPassword() {
    //given
    UserChangePasswordRequest requestBody = new UserChangePasswordRequest("password", "newPassword", "newPasswordConfirm");

    //when then
    given().log().all()
           .contentType(JSON)
           .header(AUTHORIZATION, "Bearer " + accessToken)
           .body(requestBody)
           .when()
           .put("/v1/users/me/password")
           .then().log().all()
           .statusCode(HttpStatus.CONFLICT.value());
  }

  @Test
  @DisplayName("닉네임 변경 성공시 200 응답")
  void changeNickname() {
    //given
    UserChangeNicknameRequest requestBody = new UserChangeNicknameRequest("newNickname");

    //when then
    given(this.spec)
        .filter(createChangeNicknameDocument()).log().all()
        .contentType(JSON)
        .header(AUTHORIZATION, "Bearer " + accessToken)
        .body(requestBody)
        .when()
        .patch("/v1/users/me/nickname")
        .then().log().all()
        .statusCode(HttpStatus.OK.value());
  }

  @Test
  @DisplayName("닉네임 중복시 409 응답")
  void duplicateNickname() {
    //given
    UserChangeNicknameRequest requestBody = new UserChangeNicknameRequest("TestUser");

    //when then
    given().log().all()
           .contentType(JSON)
           .header(AUTHORIZATION, "Bearer " + accessToken)
           .body(requestBody)
           .when()
           .patch("/v1/users/me/nickname")
           .then().log().all()
           .statusCode(HttpStatus.CONFLICT.value());
  }

  @Test
  @DisplayName("프로필 이미지 변경 성공시 200 응답")
  void changeProfileImage() {
    //given
    UserChangeProfileImageRequest requestBody = new UserChangeProfileImageRequest("newProfileImage.png");

    willDoNothing().given(s3Service).removeProfileImage(user.getProfileImage());

    //when then
    given(this.spec)
        .filter(createChangeProfileImageDocument()).log().all()
        .contentType(JSON)
        .header(AUTHORIZATION, "Bearer " + accessToken)
        .body(requestBody)
        .when()
        .patch("/v1/users/me/profile-image")
        .then().log().all()
        .statusCode(HttpStatus.OK.value());
  }

  @Test
  @DisplayName("기본 프로필 이미지로 변경 성공시 200 응답")
  void changeProfileImageToDefault() {
    //given
    willDoNothing().given(s3Service).removeProfileImage(user.getProfileImage());

    //when then
    given(this.spec)
        .filter(
            createChangeProfileImageToDefaultDocument()
        ).log().all()
        .contentType(JSON)
        .header(AUTHORIZATION, "Bearer " + accessToken)
        .when()
        .patch("/v1/users/me/profile-image/default")
        .then().log().all()
        .statusCode(HttpStatus.OK.value());
  }

  @Test
  @DisplayName("S3 preSignedUrl 조회")
  void getPreSignedUrl() {
    //given
    String preSignedUrl = "https://test.com/test.jpg";

    BDDMockito.given(s3Service.getPreSignedUrl(anyString())).willReturn(preSignedUrl);

    //when
    S3PreSignedUrlResponse result = given(this.spec)
        .filter(createGetPreSignedUrlDocument()).log().all()
        .contentType(JSON)
        .header(AUTHORIZATION, "Bearer " + accessToken)
        .param("extension", "jpg")
        .when()
        .get("/v1/users/me/profile-image/pre-signed")
        .then().log().all()
        .assertThat()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .body()
        .jsonPath().getObject("data", S3PreSignedUrlResponse.class);
    //then
    assertThat(result.preSignedUrl()).isEqualTo(preSignedUrl);
  }

  @Test
  @DisplayName("대여 현황 조회")
  void getActiveRentalHistory() {
    //given
    initRentalHistory();

    //when
    UserGetActiveRentalResponse result = given(this.spec)
        .filter(createGetActiveRentalHistoryDocument()).log().all()
        .contentType(JSON)
        .header(AUTHORIZATION, "Bearer " + accessToken)
        .when()
        .get("/v1/users/me/rentals/active")
        .then().log().all()
        .assertThat()
        .statusCode(HttpStatus.OK.value())
        .extract().jsonPath().getObject("data", UserGetActiveRentalResponse.class);

    //then
    assertThat(result).isNotNull();
    assertThat(result.rentals()).hasSize(2);
  }

  @Test
  @DisplayName("대여내역 조회")
  void getAllRentalHistory() {
    //given
    initRentalHistory();

    //when then
    given(this.spec)
        .filter(createGetAllRentalHistoryDocument()).log().all()
        .contentType(JSON)
        .header(AUTHORIZATION, "Bearer " + accessToken)
        .when()
        .get("/v1/users/me/rentals")
        .then().log().all()
        .assertThat()
        .statusCode(HttpStatus.OK.value())
        .body("data.content", hasSize(10))
        .body("data.page.size", equalTo(10))
        .body("data.page.totalElements", equalTo(20))
        .body("data.page.totalPages", equalTo(2));
  }

  @Test
  @DisplayName("북마크 스테이션 조회")
  void getBookmarkStations() {
    //given
    IntStream.range(0, 10)
             .mapToObj(i -> entityFixture.createStation("테스트 스테이션" + i))
             .forEach(station -> entityFixture.createBookmark(user, station));

    //when then
    given(this.spec)
        .filter(createGetBookmarkStationsDocument()).log().all()
        .contentType(JSON)
        .header(AUTHORIZATION, "Bearer " + accessToken)
        .when()
        .get("/v1/users/me/stations/bookmark")
        .then().log().all()
        .assertThat()
        .statusCode(HttpStatus.OK.value())
        .body("data.bookmarks", hasSize(10));
  }


  // 북마크 스테이션 삭제
  @Test
  @DisplayName("북마크 스테이션 삭제 성공시 200 응답")
  void removeBookmarkStation() {
    //given
    RentalStations rentalStations = entityFixture.createStation("테스트 스테이션");
    BookmarkStations bookmarkStations = entityFixture.createBookmark(user, rentalStations);
    Long bookmarkId = bookmarkStations.getId();

    //when then
    given(this.spec)
        .filter(createRemoveBookmarkstationDocument()).log().all()
        .contentType(JSON)
        .header(AUTHORIZATION, "Bearer " + accessToken)
        .when()
        .delete("/v1/users/me/stations/bookmark/{bookmarkId}", bookmarkId)
        .then().log().all()
        .assertThat()
        .statusCode(HttpStatus.OK.value());
  }

  // 삭제 대상 북마크 미존재시 404 응답
  @Test
  @DisplayName("북마크 삭제대상 미 존재시 404 응답")
  void notExistTargetBookmark() {
    //given
    Long bookmarkId = 100L;

    //when then
    given().log().all()
           .contentType(JSON)
           .header(AUTHORIZATION, "Bearer " + accessToken)
           .when()
           .delete("/v1/users/me/stations/bookmark/{bookmarkId}", bookmarkId)
           .then().log().all()
           .assertThat()
           .statusCode(HttpStatus.NOT_FOUND.value());
  }


  private void initRentalHistory() {
    RentalItemTypes rentalItemTypes = entityFixture.createItemType("65W충전기", 1000, RentalItemCategory.CHARGER);
    RentalItems rentalItems = entityFixture.createItem(RentalItemStatus.RENTED, null, rentalItemTypes);
    LocalDateTime now = LocalDateTime.now();
    for (int i = 0; i < 2; i++) {
      entityFixture.createRentalHistory(RentalStatus.RENTAL, user, rentalItems, null, now.plusHours(1));
    }
    for (int i = 0; i < 18; i++) {
      entityFixture.createRentalHistory(RentalStatus.RETURNED, user, rentalItems, null, now.minusDays(i + 1));
    }
  }

  private RestDocumentationFilter createGetUserInfoDocument() {
    return document(DEFAULT_RESTDOC_PATH,
        resource(ResourceSnippetParameters.builder()
                                          .tag(this.getClass().getSimpleName().replace("Test", ""))
                                          .summary("회원 정보 조회(ME) API")
                                          .build()
        ),
        requestHeaders(
            headerWithName(AUTHORIZATION).description("Bearer Token")
        ),
        responseFields(
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
            fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
            fieldWithPath("data.nickname").type(JsonFieldType.STRING).description("닉네임"),
            fieldWithPath("data.profileImage").type(JsonFieldType.STRING).description("프로필 이미지"),
            fieldWithPath("data.isDefaultProfileImage").type(JsonFieldType.BOOLEAN).description("기본 프로필 이미지 여부")
        )
    );
  }

  private @NotNull RestDocumentationFilter createChangePasswordDocument() {
    return document(DEFAULT_RESTDOC_PATH,
        resource(ResourceSnippetParameters.builder()
                                          .tag(this.getClass().getSimpleName().replace("Test", ""))
                                          .summary("비밀번호 재설정(로그인) API")
                                          .build()
        ),
        requestHeaders(
            headerWithName(AUTHORIZATION).description("Bearer Token")
        ),
        requestFields(
            fieldWithPath("currentPassword").type(JsonFieldType.STRING).description("현재 비밀번호"),
            fieldWithPath("newPassword").type(JsonFieldType.STRING).description("새로운 비밀번호"),
            fieldWithPath("newPasswordConfirm").type(JsonFieldType.STRING).description("새로운 비밀번호 확인")
        ),
        responseFields(
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
            fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터")
        )
    );
  }

  private @NotNull RestDocumentationFilter createChangeNicknameDocument() {
    return document(DEFAULT_RESTDOC_PATH,
        resource(ResourceSnippetParameters.builder()
                                          .tag(this.getClass().getSimpleName().replace("Test", ""))
                                          .summary("닉네임 변경 API")
                                          .build()
        ),
        requestHeaders(
            headerWithName(AUTHORIZATION).description("Bearer Token")
        ),
        requestFields(
            fieldWithPath("nickname").type(JsonFieldType.STRING).description("변경할 닉네임")
        ),
        responseFields(
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
            fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터")
        )
    );
  }

  private RestDocumentationFilter createChangeProfileImageDocument() {
    return document(DEFAULT_RESTDOC_PATH,
        resource(ResourceSnippetParameters.builder()
                                          .tag(this.getClass().getSimpleName().replace("Test", ""))
                                          .summary("프로필 이미지 변경 API")
                                          .build()
        ),
        requestHeaders(
            headerWithName(AUTHORIZATION).description("Bearer Token")
        ),
        requestFields(
            fieldWithPath("imageUrl").type(JsonFieldType.STRING).description("변경할 프로필 이미지 URL")
        ),
        responseFields(
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
            fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터")
        )
    );
  }

  private RestDocumentationFilter createChangeProfileImageToDefaultDocument() {
    return document(DEFAULT_RESTDOC_PATH,
        resource(ResourceSnippetParameters.builder()
                                          .tag(this.getClass().getSimpleName().replace("Test", ""))
                                          .summary("프로필 이미지 초기화 API")
                                          .build()
        ),
        requestHeaders(
            headerWithName(AUTHORIZATION).description("Bearer Token")
        ),
        responseFields(
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
            fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터")
        )
    );
  }

  private RestDocumentationFilter createGetPreSignedUrlDocument() {
    return document(DEFAULT_RESTDOC_PATH,
        resource(ResourceSnippetParameters.builder()
                                          .tag(this.getClass().getSimpleName().replace("Test", ""))
                                          .summary("S3 PreSignedURL 조회 API")
                                          .build()
        ),
        requestHeaders(
            headerWithName(AUTHORIZATION).description("Bearer Token")
        ),
        responseFields(
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
            fieldWithPath("data.preSignedUrl").type(JsonFieldType.STRING).description("S3 PreSignedURL")
        )
    );
  }

  private @NotNull RestDocumentationFilter createGetActiveRentalHistoryDocument() {
    return document(DEFAULT_RESTDOC_PATH,
        resource(ResourceSnippetParameters.builder()
                                          .tag(this.getClass().getSimpleName().replace("Test", ""))
                                          .summary("대여 현황 조회 API")
                                          .build()
        ),
        requestHeaders(
            headerWithName(AUTHORIZATION).description("Bearer Token")
        ),
        responseFields(
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
            fieldWithPath("data.rentals").type(JsonFieldType.ARRAY).description("대여 현황 목록"),
            fieldWithPath("data.rentals[].name").type(JsonFieldType.STRING).description("대여 물품 이름"),
            fieldWithPath("data.rentals[].status").type(JsonFieldType.STRING).description("대여 상태"),
            fieldWithPath("data.rentals[].rentalTimeHour").type(JsonFieldType.NUMBER).description("대여 시간(시간)"),
            fieldWithPath("data.rentals[].startTime").type(JsonFieldType.STRING).description("대여 시작 시간"),
            fieldWithPath("data.rentals[].expectedReturnTime").type(JsonFieldType.STRING).description("예상 반납 시간"),
            fieldWithPath("data.rentals[].token").type(JsonFieldType.STRING).description("대여내역 토큰")
        )
    );
  }

  private @NotNull RestDocumentationFilter createGetAllRentalHistoryDocument() {
    return document(DEFAULT_RESTDOC_PATH,
        resource(ResourceSnippetParameters.builder()
                                          .tag(this.getClass().getSimpleName().replace("Test", ""))
                                          .summary("대여 내역 조회 API")
                                          .build()
        ),
        requestHeaders(
            headerWithName(AUTHORIZATION).description("Bearer Token")
        ),
        responseFields(
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
            fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("대여 내역 목록"),
            fieldWithPath("data.content[].name").type(JsonFieldType.STRING).description("대여 물품 이름"),
            fieldWithPath("data.content[].status").type(JsonFieldType.STRING).description("대여 상태"),
            fieldWithPath("data.content[].rentalTimeHour").type(JsonFieldType.NUMBER).description("대여 시간(시간)"),
            fieldWithPath("data.content[].startTime").type(JsonFieldType.STRING).description("대여 시작 시간"),
            fieldWithPath("data.content[].expectedReturnTime").type(JsonFieldType.STRING).description("예상 반납 시간"),
            fieldWithPath("data.content[].token").type(JsonFieldType.STRING).description("대여내역 토큰"),
            fieldWithPath("data.page.number").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
            fieldWithPath("data.page.size").type(JsonFieldType.NUMBER).description("페이지 크기"),
            fieldWithPath("data.page.totalElements").type(JsonFieldType.NUMBER).description("전체 요소 수"),
            fieldWithPath("data.page.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수")
        )
    );
  }

  private RestDocumentationFilter createGetBookmarkStationsDocument() {
    return document(DEFAULT_RESTDOC_PATH,
        resource(ResourceSnippetParameters.builder()
                                          .tag(this.getClass().getSimpleName().replace("Test", ""))
                                          .summary("북마크 스테이션 조회 API")
                                          .build()
        ),
        requestHeaders(
            headerWithName(AUTHORIZATION).description("Bearer Token")
        ),
        responseFields(
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
            fieldWithPath("data.bookmarks").type(JsonFieldType.ARRAY).description("북마크 스테이션 목록"),
            fieldWithPath("data.bookmarks[].name").type(JsonFieldType.STRING).description("대여 스테이션 이름"),
            fieldWithPath("data.bookmarks[].stationId").type(JsonFieldType.NUMBER).description("북마크 스테이션 ID"),
            fieldWithPath("data.bookmarks[].bookmarkId").type(JsonFieldType.NUMBER).description("북마크 ID")
        )
    );
  }

  private RestDocumentationFilter createRemoveBookmarkstationDocument() {
    return document(DEFAULT_RESTDOC_PATH,
        resource(ResourceSnippetParameters.builder()
                                          .tag(this.getClass().getSimpleName().replace("Test", ""))
                                          .summary("북마크 스테이션 삭제 API")
                                          .build()
        ),
        requestHeaders(
            headerWithName(AUTHORIZATION).description("Bearer Token")
        ),
        responseFields(
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
            fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터")
        )
    );
  }

}