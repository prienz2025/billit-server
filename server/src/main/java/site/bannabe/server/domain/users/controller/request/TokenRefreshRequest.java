package site.bannabe.server.domain.users.controller.request;

public record TokenRefreshRequest(
    String refreshToken
) {

}