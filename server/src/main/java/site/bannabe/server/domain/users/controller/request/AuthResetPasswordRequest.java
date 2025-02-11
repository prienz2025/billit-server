package site.bannabe.server.domain.users.controller.request;

public record AuthResetPasswordRequest(
    String authCode,
    String email,
    String newPassword,
    String newPasswordConfirm
) {

}