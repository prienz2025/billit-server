package site.bannabe.server.global.security.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Objects;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import site.bannabe.server.global.exceptions.BannabeAuthenticationException;
import site.bannabe.server.global.exceptions.ErrorCode;
import site.bannabe.server.global.security.handler.LoginFailureHandler;
import site.bannabe.server.global.security.handler.LoginSuccessHandler;
import site.bannabe.server.global.utils.JsonUtils;

@Component
public class JSONUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

  private static final String LOGIN_URL = "/v1/auth/login";

  private static final AntPathRequestMatcher LOGIN_REQUEST_MATCHER = new AntPathRequestMatcher(LOGIN_URL, HttpMethod.POST.name());

  private final JsonUtils jsonUtils;

  protected JSONUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager, JsonUtils jsonUtils,
      LoginSuccessHandler loginSuccessHandler, LoginFailureHandler loginFailureHandler) {
    super(LOGIN_REQUEST_MATCHER, authenticationManager);
    setAuthenticationSuccessHandler(loginSuccessHandler);
    setAuthenticationFailureHandler(loginFailureHandler);
    this.jsonUtils = jsonUtils;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException, IOException {

    String contentType = request.getContentType();
    validateContentType(contentType);

    LoginRequest loginRequest = jsonUtils.deserializedJsonToObject(request.getInputStream(), LoginRequest.class);
    validateLoginRequest(loginRequest);

    Authentication authentication = createAuthentication(loginRequest);
    storeDeviceToken(request.getSession(), loginRequest);

    return super.getAuthenticationManager().authenticate(authentication);
  }

  private void validateContentType(final String requestContentType) {
    if (Objects.isNull(requestContentType) || !requestContentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
      throw new BannabeAuthenticationException(ErrorCode.UNSUPPORTED_CONTENT_TYPE);
    }
  }

  private void validateLoginRequest(LoginRequest loginRequest) {
    if (!StringUtils.hasText(loginRequest.email()) || !StringUtils.hasText(loginRequest.password())) {
      throw new BannabeAuthenticationException(ErrorCode.MISSING_REQUIRED_FIELDS);
    }
  }

  private Authentication createAuthentication(LoginRequest loginRequest) {
    String email = loginRequest.email();
    String password = loginRequest.password();
    return new UsernamePasswordAuthenticationToken(email, password);
  }

  private void storeDeviceToken(HttpSession session, LoginRequest loginRequest) {
    session.setAttribute("deviceToken", loginRequest.deviceToken());
  }

  public record LoginRequest(
      String email,
      String password,
      String deviceToken
  ) {

  }

}