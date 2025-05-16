package io.billit.server.domain.users.controller.request;

public record OAuth2AuthorizationRequest(
    String accessToken,
    String deviceToken
) {

}