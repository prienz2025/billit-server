package site.bannabe.server.global.advice;

import java.util.List;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.bannabe.server.global.exceptions.BannabeAuthenticationException;
import site.bannabe.server.global.exceptions.BannabeException;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;
import site.bannabe.server.global.type.ApiResponse;
import site.bannabe.server.global.type.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<ErrorResponse>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    List<String> errorMessages = ex.getBindingResult()
                                   .getAllErrors()
                                   .stream()
                                   .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                   .toList();
    ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.VALIDATION_FAILED, errorMessages);
    return ResponseEntity.badRequest().body(ApiResponse.failure(errorResponse));
  }

  @ExceptionHandler(BannabeAuthenticationException.class)
  public ResponseEntity<ApiResponse<ErrorResponse>> handleBannabeAuthenticationException(BannabeAuthenticationException ex) {
    return buildErrorResponse(ex);
  }

  @ExceptionHandler(BannabeServiceException.class)
  public ResponseEntity<ApiResponse<ErrorResponse>> handleBannabeServiceException(BannabeServiceException ex) {
    return buildErrorResponse(ex);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<?>> handleException(Exception ex) {
    return ResponseEntity.badRequest().body(ApiResponse.failure(ex.getMessage()));
  }

  private ResponseEntity<ApiResponse<ErrorResponse>> buildErrorResponse(BannabeException ex) {
    ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode());
    return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(ApiResponse.failure(errorResponse));
  }

}