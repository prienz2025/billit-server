package site.bannabe.server.global.type;

import java.util.List;
import site.bannabe.server.global.exceptions.ErrorCode;

public record ErrorResponse(
    String status,
    String message,
    List<String> details
) {

  public static ErrorResponse of(ErrorCode errorCode) {
    return new ErrorResponse(errorCode.name(), errorCode.getMessage(), List.of());
  }

  public static ErrorResponse of(ErrorCode errorCode, List<String> details) {
    return new ErrorResponse(errorCode.name(), errorCode.getMessage(), details);
  }

}