package site.bannabe.server.global.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import site.bannabe.server.global.exceptions.BannabeAuthenticationException;
import site.bannabe.server.global.exceptions.ErrorCode;
import site.bannabe.server.global.utils.ErrorResponseWriter;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements org.springframework.security.web.AuthenticationEntryPoint {

  private final ErrorResponseWriter errorResponseWriter;

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
      throws IOException {
    ErrorCode errorCode = (authException instanceof BannabeAuthenticationException bannabeAuthenticationException) ?
        bannabeAuthenticationException.getErrorCode() : ErrorCode.INTERNAL_SERVER_ERROR;

    if (!(authException instanceof BannabeAuthenticationException)) {
      log.error("Authentication EntryPoint Error: {} (URI: {})", authException.getMessage(), request.getRequestURI(),
          authException);
    }

    errorResponseWriter.writeErrorResponse(response, errorCode);
  }

}