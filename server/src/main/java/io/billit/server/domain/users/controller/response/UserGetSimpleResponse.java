package io.billit.server.domain.users.controller.response;

import io.billit.server.domain.users.entity.Users;

public record UserGetSimpleResponse(
    String email,
    String nickname,
    String profileImage,
    Boolean isDefaultProfileImage
) {

  public static UserGetSimpleResponse of(Users user, String defaultProfileImage) {
    return new UserGetSimpleResponse(
        user.getEmail(),
        user.getNickname(),
        user.getProfileImage(),
        user.getProfileImage().equals(defaultProfileImage)
    );
  }

}