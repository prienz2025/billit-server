package site.bannabe.server.global.utils;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import site.bannabe.server.global.exceptions.ErrorCode;
import site.bannabe.server.global.type.ApiResponse;
import site.bannabe.server.global.type.ErrorResponse;

@Component
@RequiredArgsConstructor
public class ErrorResponseWriter {

  private final JsonUtils jsonUtils;

  public void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
    ApiResponse<ErrorResponse> failure = ApiResponse.failure(ErrorResponse.of(errorCode));
    String body = jsonUtils.serializedObjectToJson(failure);

    response.setStatus(errorCode.getHttpStatus().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.getWriter().write(body);
  }

}