package site.bannabe.server.global.exceptions;

public class BannabeServiceException extends RuntimeException implements BannabeException {

  private final ErrorCode errorCode;

  public BannabeServiceException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  @Override
  public ErrorCode getErrorCode() {
    return errorCode;
  }

}