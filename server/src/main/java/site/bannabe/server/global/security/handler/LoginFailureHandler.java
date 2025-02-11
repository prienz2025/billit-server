package site.bannabe.server.global.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import site.bannabe.server.global.exceptions.BannabeAuthenticationException;
import site.bannabe.server.global.exceptions.ErrorCode;
import site.bannabe.server.global.utils.ErrorResponseWriter;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

  private final ErrorResponseWriter errorResponseWriter;

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException {
    request.getSession().invalidate();
    ErrorCode errorCode;

    if (exception instanceof BannabeAuthenticationException authException) {
      errorCode = authException.getErrorCode();
    } else if (exception instanceof BadCredentialsException) {
      errorCode = ErrorCode.INVALID_LOGIN_CREDENTIALS;
    } else if (exception instanceof UsernameNotFoundException) {
      errorCode = ErrorCode.USER_NOT_FOUND;
    } else {
      errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
      log.error("AuthenticationFailure Error! ReqeustURI: {}", request.getRequestURI(), exception);
    }

    errorResponseWriter.writeErrorResponse(response, errorCode);
  }

}