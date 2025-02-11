package site.bannabe.server.domain.rentals.controller;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.restassured.RestDocumentationFilter;
import site.bannabe.server.config.AbstractIntegrationTest;
import site.bannabe.server.domain.rentals.controller.response.RentalItemTypeDetailResponse;
import site.bannabe.server.domain.rentals.controller.response.RentalStationSimpleResponse;
import site.bannabe.server.domain.rentals.entity.RentalItemCategory;
import site.bannabe.server.domain.rentals.entity.RentalItemTypes;
import site.bannabe.server.domain.rentals.entity.RentalStationItems;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.users.entity.Users;
import site.bannabe.server.global.jwt.GenerateToken;
import site.bannabe.server.global.jwt.JwtProvider;
import site.bannabe.server.util.EntityFixture;

class RentalStationControllerTest extends AbstractIntegrationTest {

  @Autowired
  private EntityFixture entityFixture;
  @Autowired
  private JwtProvider jwtProvider;


  @AfterEach
  void tearDown() {
    entityFixture.clearAll();
  }

  @Test
  @DisplayName("전체 대여소 정보를 조회한다.")
  void getAllRentalStation() {
    //given
    for (int i = 0; i < 20; i++) {
      entityFixture.createStation("테스트 스테이션" + i);
    }

    //when
    RentalStationSimpleResponse result = given(this.spec)
        .filter(createGetAllRentalStationDocument()).log().all()
        .contentType(ContentType.JSON)
        .when()
        .get("/v1/stations")
        .then().log().all()
        .assertThat()
        .statusCode(HttpStatus.OK.value())
        .extract().jsonPath().getObject("data", RentalStationSimpleResponse.class);

    //then
    assertThat(result).isNotNull();
    assertThat(result.stations()).hasSize(20);
  }

  @Test
  @DisplayName("stationId 입력시 대여소 상세정보를 조회한다.")
  void getRentalStationDetail() {
    //given
    RentalItemTypes itemType1 = entityFixture.createItemType("65W 충전기", 1000, RentalItemCategory.CHARGER);
    RentalItemTypes itemType2 = entityFixture.createItemType("100W 충전기", 1000, RentalItemCategory.CHARGER);
    RentalStations station = entityFixture.createStation("테스트 스테이션");
    entityFixture.createStationItem(itemType1, station);
    entityFixture.createStationItem(itemType2, station);

    //when then
    given(this.spec)
        .filter(createGetRentalStationDetailDocument()).log().all()
        .log().all()
        .contentType(ContentType.JSON)
        .when()
        .get("/v1/stations/{stationId}", station.getId())
        .then().log().all()
        .assertThat()
        .statusCode(HttpStatus.OK.value())
        .body("data.rentalItems", hasSize(2));
  }

  @Test
  @DisplayName("stationId와 itemTypeId를 통해 물품 상세정보를 조회한다.")
  void getRentalItemDetail() {
    //given
    RentalItemTypes itemType = entityFixture.createItemType("65W 충전기", 1000, RentalItemCategory.CHARGER);
    RentalStations station = entityFixture.createStation("테스트 스테이션");
    RentalStationItems rentalStationItems = entityFixture.createStationItem(itemType, station);

    //when
    RentalItemTypeDetailResponse result = given(this.spec)
        .filter(createGetRentalItemDetailDocument()).log().all()
        .log().all()
        .contentType(ContentType.JSON)
        .when()
        .get("/v1/stations/{stationId}/items/{itemTypeId}", station.getId(),
            itemType.getId())
        .then().log().all()
        .assertThat()
        .statusCode(HttpStatus.OK.value())
        .extract().jsonPath().getObject("data", RentalItemTypeDetailResponse.class);

    //then
    assertThat(result).isNotNull()
                      .extracting(
                          RentalItemTypeDetailResponse::name,
                          RentalItemTypeDetailResponse::image,
                          RentalItemTypeDetailResponse::category,
                          RentalItemTypeDetailResponse::description,
                          RentalItemTypeDetailResponse::price,
                          RentalItemTypeDetailResponse::stock
                      ).containsExactly(
                          itemType.getName(),
                          itemType.getImage(),
                          itemType.getCategory().name(),
                          itemType.getDescription(),
                          itemType.getPrice(),
                          rentalStationItems.getStock()
                      );
  }

  @Test
  @DisplayName("stationId를 통해 북마크 스테이션을 추가한다.")
  void bookmarkRentalStation() {
    //given
    RentalStations station = entityFixture.createStation("테스트 스테이션");
    Users user = entityFixture.createUser("test@test.com");
    GenerateToken generateToken = jwtProvider.generateToken(user.getToken(), user.getRole().getRoleKey());

    //when then
    given(this.spec)
        .filter(createBookmarkRentalStationDocument()).log().all()
        .contentType(ContentType.JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + generateToken.accessToken())
        .when()
        .post("/v1/stations/{stationId}/bookmark", station.getId())
        .then().log().all()
        .assertThat()
        .statusCode(HttpStatus.OK.value());
  }

  @Test
  @DisplayName("이미 북마크한 스테이션 이라면 409 응답")
  void duplicateBookmark() {
    //given
    RentalStations station = entityFixture.createStation("테스트 스테이션");
    Users user = entityFixture.createUser("test@test.com");
    entityFixture.createBookmark(user, station);

    GenerateToken generateToken = jwtProvider.generateToken(user.getToken(), user.getRole().getRoleKey());

    //when then
    given().log().all()
           .contentType(ContentType.JSON)
           .header(HttpHeaders.AUTHORIZATION, "Bearer " + generateToken.accessToken())
           .when()
           .post("/v1/stations/{stationId}/bookmark", station.getId())
           .then().log().all()
           .assertThat()
           .statusCode(HttpStatus.CONFLICT.value());

  }

  private RestDocumentationFilter createGetAllRentalStationDocument() {
    return document(DEFAULT_RESTDOC_PATH,
        resource(ResourceSnippetParameters.builder()
                                          .tag(this.getClass().getSimpleName().replace("Test", ""))
                                          .summary("전체 대여 스테이션 조회 API")
                                          .build()
        ),
        responseFields(
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
            fieldWithPath("data.stations").type(JsonFieldType.ARRAY).description("대여 스테이션 목록"),
            fieldWithPath("data.stations[].name").type(JsonFieldType.STRING).description("대여 스테이션 이름"),
            fieldWithPath("data.stations[].address").type(JsonFieldType.STRING).description("대여 스테이션 주소"),
            fieldWithPath("data.stations[].latitude").type(JsonFieldType.NUMBER).description("대여 스테이션 위도"),
            fieldWithPath("data.stations[].longitude").type(JsonFieldType.NUMBER).description("대여 스테이션 경도"),
            fieldWithPath("data.stations[].status").type(JsonFieldType.STRING).description("대여 스테이션 상태"),
            fieldWithPath("data.stations[].businessTime").type(JsonFieldType.STRING).description("대여 스테이션 영업시간"),
            fieldWithPath("data.stations[].grade").type(JsonFieldType.STRING).description("대여 스테이션 등급"),
            fieldWithPath("data.stations[].stationId").type(JsonFieldType.NUMBER).description("대여 스테이션 ID")
        )
    );
  }

  private RestDocumentationFilter createGetRentalStationDetailDocument() {
    return document(DEFAULT_RESTDOC_PATH,
        resource(ResourceSnippetParameters.builder()
                                          .tag(this.getClass().getSimpleName().replace("Test", ""))
                                          .summary("대여 스테이션 상세정보 조회 API")
                                          .build()
        ),
        pathParameters(
            parameterWithName("stationId").description("대여 스테이션 ID")
        ),
        responseFields(
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
            fieldWithPath("data.rentalItems").type(JsonFieldType.ARRAY).description("대여 물품 목록"),
            fieldWithPath("data.rentalItems[].itemTypeId").type(JsonFieldType.NUMBER).description("대여 물품 ID"),
            fieldWithPath("data.rentalItems[].name").type(JsonFieldType.STRING).description("대여 물품 이름"),
            fieldWithPath("data.rentalItems[].image").type(JsonFieldType.STRING).description("대여 물품 이미지"),
            fieldWithPath("data.rentalItems[].category").type(JsonFieldType.STRING).description("대여 물품 카테고리"),
            fieldWithPath("data.rentalItems[].stock").type(JsonFieldType.NUMBER).description("대여 물품 재고")
        )
    );
  }

  private RestDocumentationFilter createGetRentalItemDetailDocument() {
    return document(DEFAULT_RESTDOC_PATH,
        resource(ResourceSnippetParameters.builder()
                                          .tag(this.getClass().getSimpleName().replace("Test", ""))
                                          .summary("대여 물품 상세정보 조회 API")
                                          .build()
        ),
        pathParameters(
            parameterWithName("stationId").description("대여 스테이션 ID"),
            parameterWithName("itemTypeId").description("대여 물품 ID")
        ),
        responseFields(
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
            fieldWithPath("data.name").type(JsonFieldType.STRING).description("대여 물품 이름"),
            fieldWithPath("data.image").type(JsonFieldType.STRING).description("대여 물품 이미지"),
            fieldWithPath("data.category").type(JsonFieldType.STRING).description("대여 물품 카테고리"),
            fieldWithPath("data.description").type(JsonFieldType.STRING).description("대여 물품 설명"),
            fieldWithPath("data.price").type(JsonFieldType.NUMBER).description("대여 물품 가격"),
            fieldWithPath("data.stock").type(JsonFieldType.NUMBER).description("대여 물품 재고")
        )
    );
  }

  private RestDocumentationFilter createBookmarkRentalStationDocument() {
    return document(DEFAULT_RESTDOC_PATH,
        resource(ResourceSnippetParameters.builder()
                                          .tag(this.getClass().getSimpleName().replace("Test", ""))
                                          .summary("스테이션 북마크 API")
                                          .build()
        ),
        requestHeaders(
            headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer Token")
        ),
        pathParameters(
            parameterWithName("stationId").description("대여 스테이션 ID")
        ),
        responseFields(
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
            fieldWithPath("data").type(JsonFieldType.OBJECT).description("데이터")
        )
    );
  }

}