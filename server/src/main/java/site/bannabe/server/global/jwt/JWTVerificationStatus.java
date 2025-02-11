package site.bannabe.server.global.jwt;

public enum JWTVerificationStatus {

  VALID, INVALID, EXPIRED, UNSUPPORTED;

  public boolean isNotValid() {
    return !this.equals(VALID);
  }

}