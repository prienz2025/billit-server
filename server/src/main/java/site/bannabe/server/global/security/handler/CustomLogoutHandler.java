package site.bannabe.server.global.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import site.bannabe.server.domain.users.controller.request.TokenRefreshRequest;
import site.bannabe.server.global.exceptions.BannabeAuthenticationException;
import site.bannabe.server.global.exceptions.ErrorCode;
import site.bannabe.server.global.jwt.JwtService;
import site.bannabe.server.global.utils.JsonUtils;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

  private static final String PREFIX = "Bearer ";
  private final JwtService jwtService;
  private final JsonUtils jsonUtils;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith(PREFIX)) {
      throw new BannabeAuthenticationException(ErrorCode.TOKEN_NOT_FOUND);
    }
    String accessToken = authHeader.substring(PREFIX.length());
    String refreshToken = getRefreshToken(request);
    jwtService.removeRefreshToken(accessToken, refreshToken);
  }

  private String getRefreshToken(HttpServletRequest request) {
    try {
      return jsonUtils.deserializedJsonToObject(request.getInputStream(), TokenRefreshRequest.class).refreshToken();
    } catch (IOException e) {
      throw new BannabeAuthenticationException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }
  }

}