package io.billit.server.domain.users.controller.request;

public record TokenRefreshRequest(
    String refreshToken
) {

}