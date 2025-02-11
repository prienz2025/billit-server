package site.bannabe.server.domain.users.controller.request;

public record OAuth2AuthorizationRequest(
    String accessToken,
    String deviceToken
) {

}