package site.bannabe.server.domain.users.entity;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.bannabe.server.global.type.BaseEntity;
import site.bannabe.server.global.utils.RandomCodeGenerator;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Users extends BaseEntity {

  private String email;

  private String password;

  private String profileImage;

  @Default
  private String nickname = RandomCodeGenerator.generateRandomNickname();

  @Default
  private String token = RandomCodeGenerator.generateRandomToken(Users.class);

  @Default
  private Role role = Role.USER;

  @Default
  private ProviderType providerType = ProviderType.LOCAL;

  private Users(String email, String password, String profileImage, String nickname, String token, Role role,
      ProviderType providerType) {
    this.email = email;
    this.password = password;
    this.profileImage = profileImage;
    this.nickname = nickname;
    this.token = token;
    this.role = role;
    this.providerType = providerType;
  }

  public static Users createUser(String email, String password, String profileImage) {
    return Users.builder().email(email).password(password).profileImage(profileImage).build();
  }

  public void changePassword(String newPassword) {
    password = newPassword;
  }

  public void changeNickname(String newNickname) {
    nickname = newNickname;
  }

  public boolean isNotDefaultProfileImage(String defaultProfileImage) {
    return !profileImage.equals(defaultProfileImage);
  }

  public void changeProfileImage(String newProfileImage) {
    profileImage = newProfileImage;
  }

}