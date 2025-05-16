package io.billit.server.domain.users.controller.request;

public record AuthResetPasswordRequest(
    String authCode,
    String email,
    String newPassword,
    String newPasswordConfirm
) {

}