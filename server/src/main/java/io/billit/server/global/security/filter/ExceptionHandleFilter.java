package io.billit.server.global.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;
import io.billit.server.global.exceptions.BannabeAuthenticationException;
import io.billit.server.global.exceptions.BannabeServiceException;
import io.billit.server.global.exceptions.ErrorCode;
import io.billit.server.global.utils.ErrorResponseWriter;

@RequiredArgsConstructor
public class ExceptionHandleFilter extends OncePerRequestFilter {

  private final ErrorResponseWriter errorResponseWriter;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws IOException {
    try {
      filterChain.doFilter(request, response);
    } catch (BannabeAuthenticationException | BannabeServiceException e) {
      errorResponseWriter.writeErrorResponse(response, e.getErrorCode());
    } catch (Exception e) {
      ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
      errorResponseWriter.writeErrorResponse(response, errorCode);
    }
  }

}