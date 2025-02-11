package site.bannabe.server.global.jwt;

public record GenerateToken(
    String accessToken,
    String refreshToken
) {

}