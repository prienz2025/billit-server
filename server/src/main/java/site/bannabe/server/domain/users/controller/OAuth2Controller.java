package site.bannabe.server.domain.users.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.bannabe.server.domain.users.controller.request.OAuth2AuthorizationRequest;
import site.bannabe.server.domain.users.service.OAuth2Service;
import site.bannabe.server.global.type.TokenResponse;

@RestController
@RequestMapping("/v1/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

  private final OAuth2Service oAuth2Service;

  @PostMapping("/login/{provider}")
  public TokenResponse loginWithOAuth2(@PathVariable String provider,
      @RequestBody OAuth2AuthorizationRequest authorizationRequest) {
    return oAuth2Service.processOAuth2Login(provider, authorizationRequest);
  }

}