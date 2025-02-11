package site.bannabe.server.global.security.auth;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public record EndPoint(
    HttpMethod method,
    String pattern
) {

  public static final List<EndPoint> PERMIT_ALL = List.of(
      new EndPoint(HttpMethod.GET, "/health"),
      new EndPoint(HttpMethod.POST, "/v1/auth/register"),
      new EndPoint(HttpMethod.POST, "/v1/auth/login"),
      new EndPoint(HttpMethod.POST, "/v1/auth/token/refresh"),
      new EndPoint(HttpMethod.POST, "/v1/auth/send-code"),
      new EndPoint(HttpMethod.POST, "/v1/auth/verify-code"),
      new EndPoint(HttpMethod.PUT, "/v1/auth/reset-password"),
      new EndPoint(HttpMethod.GET, "/v1/stations"),
      new EndPoint(HttpMethod.GET, "/v1/stations/{stationId}"),
      new EndPoint(HttpMethod.GET, "/v1/stations/{stationId}/items/{itemTypeId}"),
      new EndPoint(HttpMethod.GET, "/v1/rentals/{rentalItemToken}"),
      new EndPoint(HttpMethod.GET, "/notices/**"),
      new EndPoint(HttpMethod.GET, "/events/**"),
      new EndPoint(HttpMethod.GET, "/v1/payments/success"),
      new EndPoint(HttpMethod.GET, "/v1/payments/failure"),
      new EndPoint(HttpMethod.GET, "/payment-test"),
      new EndPoint(HttpMethod.GET, "/payment-complete"),
      new EndPoint(HttpMethod.POST, "/v1/oauth2/login/{provider}"),
      new EndPoint(HttpMethod.GET, "/swagger/**")
  );

  public static final List<RequestMatcher> PERMIT_ALL_MATCHERS = Stream.concat(
      PERMIT_ALL.stream().map(EndPoint::toMatcher),
      Stream.of(PathRequest.toStaticResources().atCommonLocations())
  ).toList();

  private RequestMatcher toMatcher() {
    return AntPathRequestMatcher.antMatcher(this.method(), this.pattern());
  }

}