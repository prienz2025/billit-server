package site.bannabe.server.global.exceptions;

import org.springframework.security.core.AuthenticationException;

public class BannabeAuthenticationException extends AuthenticationException implements BannabeException {

  private final ErrorCode errorCode;

  public BannabeAuthenticationException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  @Override
  public ErrorCode getErrorCode() {
    return errorCode;
  }

}