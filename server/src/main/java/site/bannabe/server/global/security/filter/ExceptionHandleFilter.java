package site.bannabe.server.global.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;
import site.bannabe.server.global.exceptions.BannabeAuthenticationException;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;
import site.bannabe.server.global.utils.ErrorResponseWriter;

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