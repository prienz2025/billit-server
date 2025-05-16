package io.billit.server.domain.users.controller.request;

public record AuthVerifyCodeRequest(
    String email,
    String authCode
) {

}