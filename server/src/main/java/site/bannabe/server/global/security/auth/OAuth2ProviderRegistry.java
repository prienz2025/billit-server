package site.bannabe.server.global.security.auth;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

public abstract class OAuth2ProviderRegistry {

  private static final Map<String, OAuth2ProviderType> PROVIDER_TYPE_MAP =
      Collections.unmodifiableMap(
          Arrays.stream(OAuth2ProviderType.values())
                .collect(Collectors.toMap(type -> type.PROVIDER, Function.identity()))
      );


  public static OAuth2ProviderType getType(String provider) {
    return Optional.ofNullable(PROVIDER_TYPE_MAP.get(provider)).orElseThrow();
  }

  @RequiredArgsConstructor
  public enum OAuth2ProviderType {

    KAKAO("kakao", "kakao_account", "email", "profile_image_url", "https://kapi.kakao.com/v2/user/me"),
    GOOGLE("google", null, "email", "picture", "https://www.googleapis.com/oauth2/v3/userinfo");

    public final String PROVIDER;
    public final String ATTRIBUTES_FIELD;
    public final String EMAIL_FIELD;
    public final String IMAGE_FIELD;
    public final String USER_INFO_URL;

  }

}