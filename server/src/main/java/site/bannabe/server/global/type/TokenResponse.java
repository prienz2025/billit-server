package site.bannabe.server.global.type;

import site.bannabe.server.global.jwt.GenerateToken;

public record TokenResponse(String accessToken, String refreshToken) {

  public static TokenResponse create(GenerateToken generateToken) {
    return new TokenResponse(generateToken.accessToken(), generateToken.refreshToken());
  }

}