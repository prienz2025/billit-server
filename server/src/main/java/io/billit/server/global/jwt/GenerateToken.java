package io.billit.server.global.jwt;

public record GenerateToken(
    String accessToken,
    String refreshToken
) {

}