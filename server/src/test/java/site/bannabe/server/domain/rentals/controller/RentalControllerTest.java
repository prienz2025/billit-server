package site.bannabe.server.domain.rentals.controller;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static site.bannabe.server.domain.payments.entity.PaymentType.RENT;
import static site.bannabe.server.domain.rentals.entity.RentalItemStatus.AVAILABLE;
import static site.bannabe.server.domain.rentals.entity.RentalItemStatus.RENTED;
import static site.bannabe.server.domain.rentals.entity.RentalStatus.RENTAL;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.restassured.RestDocumentationFilter;
import site.bannabe.server.config.AbstractIntegrationTest;
import site.bannabe.server.domain.payments.entity.RentalPayments;
import site.bannabe.server.domain.rentals.entity.RentalHistory;
import site.bannabe.server.domain.rentals.entity.RentalItemCategory;
import site.bannabe.server.domain.rentals.entity.RentalItemTypes;
import site.bannabe.server.domain.rentals.entity.RentalItems;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.global.jwt.GenerateToken;
import site.bannabe.server.global.jwt.JwtProvider;
import site.bannabe.server.util.EntityFixture;

class RentalControllerTest extends AbstractIntegrationTest {

  @Autowired
  private EntityFixture entityFixture;
  @Autowired
  private JwtProvider jwtProvider;

  @AfterEach
  void tearDown() {
    entityFixture.clearAll();
  }

  @Test
  @DisplayName("rentalItemToken을 사용해 대여물품 정보를 조회한다.")
  void getRentalItemToken() {
    //given
    RentalItemTypes itemType = entityFixture.createItemType("65W 충전기", 1000, RentalItemCategory.CHARGER);
    RentalStations station = entityFixture.createStation("테스트 스테이션");
    RentalItems item = entityFixture.createItem(AVAILABLE, station, itemType);

    GenerateToken generateToken = jwtProvider.generateToken("entityToken", "ROLE_USER");

    //when
    given(this.spec)
        .filter(createGetRentalItemInfoDocument())
        .log().all()
        .contentType(JSON)
        .header(AUTHORIZATION, "Bearer " + generateToken.accessToken())
        .when()
        .get("/v1/rentals/{rentalItemToken}", item.getToken())
        .then().log().all()
        .assertThat()
        .statusCode(HttpStatus.OK.value())
        .body("data.name", equalTo(itemType.getName()))
        .body("data.price", equalTo(itemType.getPrice()))
        .body("data.currentStationName", equalTo(station.getName()));

    //then
  }

  @Test
  @DisplayName("대여 성공시 rentalItemToken을 사용해 대여내역 정보를 조회한다.")
  void getRentalHistoryInfo() {
    //given
    RentalItemTypes itemType = entityFixture.createItemType("65W 충전기", 1000, RentalItemCategory.CHARGER);
    RentalStations station = entityFixture.createStation("테스트 스테이션");
    RentalItems item = entityFixture.createItem(RENTED, station, itemType);
    RentalHistory rentalHistory = entityFixture.createRentalHistory(RENTAL, null, item, station, LocalDateTime.now());
    RentalPayments rentalPayments = entityFixture.createPayment(RENT, "테스트주문", 1000, rentalHistory);
    GenerateToken generateToken = jwtProvider.generateToken("entityToken", "ROLE_USER");

    //when then
    given(this.spec)
        .filter(createGetRentalHistoryInfoDocument())
        .log().all()
        .contentType(JSON)
        .header(AUTHORIZATION, "Bearer " + generateToken.accessToken())
        .when()
        .get("/v1/rentals/success/{rentalHistoryToken}", rentalHistory.getToken())
        .then().log().all()
        .assertThat()
        .statusCode(HttpStatus.OK.value())
        .body("data.totalAmount", equalTo(rentalPayments.getTotalAmount()))
        .body("data.itemName", equalTo(itemType.getName()))
        .body("data.rentalTime", equalTo(rentalHistory.getRentalTimeHour()))
        .body("data.rentalStationName", equalTo(station.getName()));
  }

  private RestDocumentationFilter createGetRentalItemInfoDocument() {
    return document(DEFAULT_RESTDOC_PATH,
        resource(ResourceSnippetParameters.builder()
                                          .tag(this.getClass().getSimpleName().replace("Test", ""))
                                          .summary("대여물품 조회 API")
                                          .build()
        ),
        requestHeaders(
            headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer Token")
        ),
        pathParameters(
            parameterWithName("rentalItemToken").description("대여물품 토큰")
        ),
        responseFields(
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("응답 성공 여부"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
            fieldWithPath("data.name").type(JsonFieldType.STRING).description("대여물품 이름"),
            fieldWithPath("data.price").type(JsonFieldType.NUMBER).description("대여물품 가격"),
            fieldWithPath("data.currentStationName").type(JsonFieldType.STRING).description("대여물품 현재 위치")
        )
    );
  }

  private RestDocumentationFilter createGetRentalHistoryInfoDocument() {
    return document(DEFAULT_RESTDOC_PATH,
        resource(ResourceSnippetParameters.builder()
                                          .tag(this.getClass().getSimpleName().replace("Test", ""))
                                          .summary("대여내역 조회 API")
                                          .build()
        ),
        requestHeaders(
            headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer Token")
        ),
        pathParameters(
            parameterWithName("rentalHistoryToken").description("대여내역 토큰")
        ),
        responseFields(
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("응답 성공 여부"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
            fieldWithPath("data.totalAmount").type(JsonFieldType.NUMBER).description("대여금액"),
            fieldWithPath("data.itemName").type(JsonFieldType.STRING).description("대여물품 이름"),
            fieldWithPath("data.rentalTime").type(JsonFieldType.NUMBER).description("대여시간"),
            fieldWithPath("data.rentalStationName").type(JsonFieldType.STRING).description("대여 스테이션 이름")
        )
    );
  }

}