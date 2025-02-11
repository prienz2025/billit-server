package site.bannabe.server.global.security.auth;

import java.util.Map;
import site.bannabe.server.domain.users.entity.ProviderType;
import site.bannabe.server.domain.users.entity.Users;
import site.bannabe.server.global.security.auth.OAuth2ProviderRegistry.OAuth2ProviderType;

public record OAuth2UserInfo(
    ProviderType providerType,
    String email,
    String profileImage
) {

  @SuppressWarnings("unchecked")
  public static OAuth2UserInfo from(OAuth2ProviderType oauth2Type, Map<String, Object> attributes) {
    ProviderType providerType = ProviderType.valueOf(oauth2Type.name());
    String email;
    String profileImage;

    switch (oauth2Type) {
      case KAKAO -> {
        Map<String, Object> nestedAttributes = (Map<String, Object>) attributes.get(oauth2Type.ATTRIBUTES_FIELD);
        Map<String, Object> profileAttributes = (Map<String, Object>) nestedAttributes.get("profile");
        email = (String) nestedAttributes.get(oauth2Type.EMAIL_FIELD);
        profileImage = (String) profileAttributes.get(oauth2Type.IMAGE_FIELD);
      }
      case GOOGLE -> {
        email = (String) attributes.get(oauth2Type.EMAIL_FIELD);
        profileImage = (String) attributes.get(oauth2Type.IMAGE_FIELD);
      }
      default -> throw new IllegalStateException("Unexpected value: " + oauth2Type);
    }

    return new OAuth2UserInfo(providerType, email, profileImage);
  }

  public Users toUser() {
    return Users.builder()
                .email(email)
                .profileImage(profileImage)
                .providerType(providerType)
                .build();
  }

}