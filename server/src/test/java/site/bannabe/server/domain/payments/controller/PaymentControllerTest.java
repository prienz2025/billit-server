package site.bannabe.server.domain.payments.controller;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import io.restassured.http.ContentType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.restassured.RestDocumentationFilter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import site.bannabe.server.config.AbstractIntegrationTest;
import site.bannabe.server.domain.payments.controller.request.PaymentCalculateRequest;
import site.bannabe.server.domain.payments.controller.request.PaymentConfirmRequest;
import site.bannabe.server.domain.payments.entity.PaymentMethod;
import site.bannabe.server.domain.payments.entity.PaymentType;
import site.bannabe.server.domain.payments.service.OrderInfoService;
import site.bannabe.server.domain.rentals.entity.RentalItemCategory;
import site.bannabe.server.domain.rentals.entity.RentalItemStatus;
import site.bannabe.server.domain.rentals.entity.RentalItemTypes;
import site.bannabe.server.domain.rentals.entity.RentalItems;
import site.bannabe.server.domain.rentals.entity.RentalStations;
import site.bannabe.server.domain.users.entity.Users;
import site.bannabe.server.global.api.TossPaymentApiClient;
import site.bannabe.server.global.api.TossPaymentConfirmResponse;
import site.bannabe.server.global.api.TossPaymentConfirmResponse.ReceiptInfo;
import site.bannabe.server.global.jwt.GenerateToken;
import site.bannabe.server.global.jwt.JwtProvider;
import site.bannabe.server.util.EntityFixture;

class PaymentControllerTest extends AbstractIntegrationTest {

  @Autowired
  private JwtProvider jwtProvider;
  @Autowired
  private OrderInfoService orderInfoService;
  @Autowired
  private EntityFixture entityFixture;
  @MockitoBean
  private TossPaymentApiClient tossPaymentApiClient;

  @AfterEach
  void tearDown() {
    entityFixture.clearAll();
    orderInfoService.removeOrderInfo("orderId");
  }

  @Test
  @DisplayName("rentalItemToken과 rentalTime을 받아서 결제금액을 계산한다.")
  void calculateAmount() {
    //given
    RentalItemTypes rentalItemTypes = entityFixture.createItemType("", 1000, RentalItemCategory.CHARGER);
    RentalItems rentalItems = entityFixture.createItem(RentalItemStatus.AVAILABLE, null, rentalItemTypes);
    PaymentCalculateRequest request = new PaymentCalculateRequest(rentalItems.getToken(), 2);
    GenerateToken generateToken = jwtProvider.generateToken("entityToken", "ROLE_USER");

    //when then
    given(this.spec)
        .filter(createCalculateAmountDocument()).log().all()
        .contentType(ContentType.JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + generateToken.accessToken())
        .body(request)
        .when()
        .post("/v1/payments/calculate")
        .then().log().all()
        .assertThat()
        .statusCode(HttpStatus.OK.value())
        .body("data.amount", equalTo(rentalItemTypes.getPrice() * request.rentalTime()));
  }

  @Test
  @DisplayName("결제창 호출 URL 조회 요청 시 결제창 url을 응답한다.")
  void getCheckoutUrl() {
    //given
    GenerateToken generateToken = jwtProvider.generateToken("entityToken", "ROLE_USER");

    //when then
    given(this.spec)
        .filter(createGetCheckoutUrlDocument()).log().all()
        .log().all()
        .contentType(ContentType.JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + generateToken.accessToken())
        .when()
        .get("/v1/payments/checkout-url")
        .then().log().all()
        .assertThat()
        .statusCode(HttpStatus.OK.value())
        .body("data.checkoutUrl", equalTo("http://localhost:8080/v1/payments/checkout"));
  }

  @Test
  @DisplayName("결제승인 요청시 PG사 결제 승인과 대여내역을 저장하고, 대여내역 토큰을 응답한다.")
  void confirmPayment() {
    //given
    Users user = entityFixture.createUser("test@test.com");
    RentalItemTypes rentalItemTypes = entityFixture.createItemType("65W 충전기", 1000, RentalItemCategory.CHARGER);
    RentalStations rentalStations = entityFixture.createStation("강남역");
    RentalItems rentalItems = entityFixture.createItem(RentalItemStatus.AVAILABLE, rentalStations, rentalItemTypes);
    entityFixture.createStationItem(rentalItemTypes, rentalStations);

    GenerateToken generateToken = jwtProvider.generateToken(user.getToken(), user.getRole().getRoleKey());
    PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest("paymentKey", 1000, "orderId");
    TossPaymentConfirmResponse confirmResponse = new TossPaymentConfirmResponse("paymentKey", PaymentMethod.CARD,
        "orderId", "65W 충전기/1시간", 1000, LocalDateTime.now(),
        new ReceiptInfo("receiptUrl"));
    orderInfoService.saveOrderInfo("orderId", rentalItems.getToken(), 1, 1000, PaymentType.RENT);
    given(tossPaymentApiClient.confirmPaymentRequest(confirmRequest)).willReturn(confirmResponse);

    //when
    given(this.spec)
        .filter(createConfirmPaymentDocument()).log().all()
        .log().all()
        .contentType(ContentType.JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + generateToken.accessToken())
        .body(confirmRequest)
        .when()
        .post("/v1/payments/confirm")
        .then().log().all()
        .assertThat()
        .statusCode(HttpStatus.OK.value())
        .body("data.rentalHistoryToken", startsWith("RH_"));
  }

  private RestDocumentationFilter createCalculateAmountDocument() {
    return document(DEFAULT_RESTDOC_PATH,
        resource(ResourceSnippetParameters.builder()
                                          .tag(this.getClass().getSimpleName().replace("Test", ""))
                                          .summary("결제 금액 계산 API")
                                          .build()
        ),
        requestHeaders(
            headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer Token")
        ),
        requestFields(
            fieldWithPath("rentalItemToken").type(JsonFieldType.STRING).description("대여 아이템 토큰"),
            fieldWithPath("rentalTime").type(JsonFieldType.NUMBER).description("대여 시간")
        ),
        responseFields(
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("응답 성공 여부"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
            fieldWithPath("data.amount").type(JsonFieldType.NUMBER).description("결제 금액"),
            fieldWithPath("data.pricePerHour").type(JsonFieldType.NUMBER).description("시간당 가격"),
            fieldWithPath("data.rentalTime").type(JsonFieldType.NUMBER).description("대여 시간"),
            fieldWithPath("data.rentalItemToken").type(JsonFieldType.STRING).description("대여 물품 토큰")
        )
    );
  }

  private RestDocumentationFilter createGetCheckoutUrlDocument() {
    return document(DEFAULT_RESTDOC_PATH,
        resource(ResourceSnippetParameters.builder()
                                          .tag(this.getClass().getSimpleName().replace("Test", ""))
                                          .summary("결제창 호출 URL 조회 API")
                                          .build()
        ),
        requestHeaders(
            headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer Token")
        ),
        responseFields(
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("응답 성공 여부"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
            fieldWithPath("data.checkoutUrl").type(JsonFieldType.STRING).description("결제창 URL")
        )
    );
  }

  private RestDocumentationFilter createConfirmPaymentDocument() {
    return document(DEFAULT_RESTDOC_PATH,
        resource(ResourceSnippetParameters.builder()
                                          .tag(this.getClass().getSimpleName().replace("Test", ""))
                                          .summary("결제 승인 API")
                                          .build()
        ),
        requestHeaders(
            headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer Token")
        ),
        requestFields(
            fieldWithPath("paymentKey").type(JsonFieldType.STRING).description("결제 고유 키"),
            fieldWithPath("amount").type(JsonFieldType.NUMBER).description("결제 금액"),
            fieldWithPath("orderId").type(JsonFieldType.STRING).description("주문 번호")
        ),
        responseFields(
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("응답 성공 여부"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
            fieldWithPath("data.rentalHistoryToken").type(JsonFieldType.STRING).description("대여내역 토큰")
        )
    );
  }

}