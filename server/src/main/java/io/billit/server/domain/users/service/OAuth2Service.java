package io.billit.server.domain.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.billit.server.domain.users.controller.request.OAuth2AuthorizationRequest;
import io.billit.server.domain.users.entity.Users;
import io.billit.server.domain.users.repository.UserRepository;
import io.billit.server.global.api.OAuth2ApiClient;
import io.billit.server.global.exceptions.BannabeServiceException;
import io.billit.server.global.jwt.GenerateToken;
import io.billit.server.global.jwt.JwtService;
import io.billit.server.global.security.auth.OAuth2ProviderRegistry;
import io.billit.server.global.security.auth.OAuth2ProviderRegistry.OAuth2ProviderType;
import io.billit.server.global.security.auth.OAuth2UserInfo;
import io.billit.server.global.type.TokenResponse;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

  private final UserRepository userRepository;
  private final OAuth2ApiClient oAuth2ApiClient;
  private final JwtService jwtService;
  private final PasswordService passwordService;

  @Transactional
  public TokenResponse processOAuth2Login(String provider, OAuth2AuthorizationRequest authorizationRequest) {
    OAuth2ProviderType oAuth2ProviderType = OAuth2ProviderRegistry.getType(provider);
    OAuth2UserInfo oAuth2UserInfo = oAuth2ApiClient.getOAuth2UserInfo(oAuth2ProviderType, authorizationRequest.accessToken());
    return registerOrAuthenticateUser(oAuth2UserInfo, authorizationRequest.deviceToken());
  }

  private TokenResponse registerOrAuthenticateUser(OAuth2UserInfo oAuth2UserInfo, String deviceToken) {
    Users user;

    try {
      user = userRepository.findByEmail(oAuth2UserInfo.email());
    } catch (BannabeServiceException e) {
      user = registerNewUser(oAuth2UserInfo);
    }

    GenerateToken token = jwtService.createJWT(user.getEmail(), user.getRole().getRoleKey(), deviceToken);
    return TokenResponse.create(token);
  }

  private Users registerNewUser(OAuth2UserInfo oAuth2UserInfo) {
    Users newUser = oAuth2UserInfo.toUser();
    String encodedPassword = passwordService.encodePassword(newUser.getProviderType().name() + "_" + newUser.getEmail());
    newUser.changePassword(encodedPassword);
    return userRepository.save(newUser);
  }

}