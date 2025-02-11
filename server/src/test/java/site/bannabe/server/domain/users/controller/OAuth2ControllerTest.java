package site.bannabe.server.domain.users.controller;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.restassured.RestDocumentationFilter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import site.bannabe.server.config.AbstractIntegrationTest;
import site.bannabe.server.domain.users.controller.request.OAuth2AuthorizationRequest;
import site.bannabe.server.domain.users.entity.ProviderType;
import site.bannabe.server.global.api.OAuth2ApiClient;
import site.bannabe.server.global.security.auth.OAuth2ProviderRegistry;
import site.bannabe.server.global.security.auth.OAuth2ProviderRegistry.OAuth2ProviderType;
import site.bannabe.server.global.security.auth.OAuth2UserInfo;
import site.bannabe.server.util.EntityFixture;

class OAuth2ControllerTest extends AbstractIntegrationTest {

  @Autowired
  private EntityFixture entityFixture;
  @MockitoBean
  private OAuth2ApiClient oAuth2ApiClient;

  @AfterEach
  void tearDown() {
    entityFixture.clearAll();
  }

  @Test
  @DisplayName("소셜로그인 성공시 JWT를 응답한다.")
  void kakaoLoginSuccess() {
    //given
    String provider = "kakao";
    OAuth2ProviderType providerType = OAuth2ProviderRegistry.getType(provider);
    OAuth2AuthorizationRequest request = new OAuth2AuthorizationRequest("accessToken", "deviceToken");

    OAuth2UserInfo oAuth2UserInfo = new OAuth2UserInfo(ProviderType.KAKAO, "test@test.com", "test-image.png");
    given(oAuth2ApiClient.getOAuth2UserInfo(providerType, request.accessToken())).willReturn(oAuth2UserInfo);

    //when then
    RestAssured.given(this.spec)
               .filter(createOAuth2LoginDocument())
               .log().all()
               .contentType(ContentType.JSON)
               .body(request)
               .when()
               .post("/v1/oauth2/login/{provider}", provider)
               .then().log().all()
               .statusCode(HttpStatus.OK.value())
               .body("data.accessToken", notNullValue())
               .body("data.refreshToken", notNullValue());
  }

  private RestDocumentationFilter createOAuth2LoginDocument() {
    return document(DEFAULT_RESTDOC_PATH,
        resource(ResourceSnippetParameters.builder()
                                          .tag(this.getClass().getSimpleName().replace("Test", ""))
                                          .summary("OAuth2 인증 요청 API")
                                          .build()
        ),
        pathParameters(
            parameterWithName("provider").description("소셜로그인 Provider")
        ),
        requestFields(
            fieldWithPath("accessToken").type(JsonFieldType.STRING).description("소셜로그인 인증 토큰"),
            fieldWithPath("deviceToken").type(JsonFieldType.STRING).description("디바이스 토큰")
        ),
        responseFields(
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("응답 성공 여부"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
            fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("JWT 토큰"),
            fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("JWT 리프레시 토큰")
        )
    );
  }

}