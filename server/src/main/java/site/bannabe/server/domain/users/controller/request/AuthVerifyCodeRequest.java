package site.bannabe.server.domain.users.controller.request;

public record AuthVerifyCodeRequest(
    String email,
    String authCode
) {

}