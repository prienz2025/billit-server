package site.bannabe.server.global.api;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import site.bannabe.server.global.security.auth.OAuth2ProviderRegistry.OAuth2ProviderType;
import site.bannabe.server.global.security.auth.OAuth2UserInfo;

@Component
@RequiredArgsConstructor
public class OAuth2ApiClient {

  private final RestClient restClient;

  public OAuth2UserInfo getOAuth2UserInfo(OAuth2ProviderType providerType, String accessToken) {
    Map<String, Object> attributes = restClient.get().uri(providerType.USER_INFO_URL)
                                               .headers(headers -> {
                                                 headers.setBearerAuth(accessToken);
                                                 headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                                               })
                                               .retrieve()
                                               .body(new ParameterizedTypeReference<>() {
                                               });
    return OAuth2UserInfo.from(providerType, attributes);
  }

}