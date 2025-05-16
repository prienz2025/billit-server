package io.billit.server.global.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import io.billit.server.global.jwt.GenerateToken;
import io.billit.server.global.jwt.JwtService;
import io.billit.server.global.security.auth.PrincipalDetails;
import io.billit.server.global.type.ApiResponse;
import io.billit.server.global.type.TokenResponse;
import io.billit.server.global.utils.JsonUtils;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtService jwtService;
  private final JsonUtils jsonUtils;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    String deviceToken = (String) request.getSession().getAttribute("deviceToken");
    GenerateToken tokens = generateToken(authentication, deviceToken);
    ApiResponse<TokenResponse> apiResponse = ApiResponse.success("로그인 성공", TokenResponse.create(tokens));
    writeResponse(response, apiResponse);
    clearLoginUsedSession(request);
  }

  private GenerateToken generateToken(Authentication authentication, String deviceToken) {
    PrincipalDetails user = (PrincipalDetails) authentication.getPrincipal();
    String entityToken = user.getEntityToken();
    String authorities = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
    return jwtService.createJWT(entityToken, authorities, deviceToken);
  }

  private void writeResponse(HttpServletResponse response, ApiResponse<TokenResponse> apiResponse) throws IOException {
    String body = jsonUtils.serializedObjectToJson(apiResponse);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.getWriter().write(body);
    response.getWriter().flush();
  }

  private void clearLoginUsedSession(HttpServletRequest request) {
    SecurityContextHolder.clearContext();
    HttpSession session = request.getSession(false);
    if (Objects.nonNull(session)) {
      session.invalidate();
    }
  }

}