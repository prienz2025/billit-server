package site.bannabe.server.domain.users.service;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;

class PasswordServiceTest {

  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
  private final PasswordService passwordService = new PasswordService(encoder);

  @Test
  @DisplayName("새 비밀번호 검증 성공")
  void validateNewPassword() {
    //given
    String newPassword = "newPassword";
    String newPasswordConfirm = "newPassword";

    //when
    passwordService.validateNewPassword(newPassword, newPasswordConfirm);

    //then
    assertThatNoException().isThrownBy(() -> passwordService.validateNewPassword(newPassword, newPasswordConfirm));
  }

  @Test
  @DisplayName("새 비밀번호 불일치 시 예외 발생")
  void mismatchNewPassword() {
    //given
    String newPassword = "newPassword";
    String newPasswordConfirm = "wrongPassword";

    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> passwordService.validateNewPassword(newPassword, newPasswordConfirm))
        .withMessage(ErrorCode.NEW_PASSWORD_MISMATCH.getMessage());
  }

  @Test
  @DisplayName("비밀번호 재사용 검증 성공")
  void validateReusedPassword() {
    //given
    String newPassword = "newPassword";
    String currentPassword = encoder.encode("currentPassword");

    //when then
    assertThatNoException().isThrownBy(() -> passwordService.validateReusedPassword(newPassword, currentPassword));
  }

  @Test
  @DisplayName("비밀번호 재사용시 예외 발생")
  void reusedPassword() {
    //given
    String newPassword = "currentPassword";
    String currentPassword = encoder.encode("currentPassword");

    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> passwordService.validateReusedPassword(newPassword, currentPassword))
        .withMessage(ErrorCode.DUPLICATE_PASSWORD.getMessage());
  }

  @Test
  @DisplayName("기존 비밀번호 일치 검증")
  void validateCurrentPassword() {
    //given
    String currentPassword = "currentPassword";
    String encodedPassword = encoder.encode(currentPassword);

    //when then
    assertThatNoException().isThrownBy(() -> passwordService.validateCurrentPassword(currentPassword, encodedPassword));
  }


  @Test
  @DisplayName("기존 비밀번호 불일치시 예외 발생")
  void mismatchCurrentPassword() {
    //given
    String currentPassword = "currentPassword";
    String encodedPassword = encoder.encode("wrongPassword");

    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> passwordService.validateCurrentPassword(currentPassword, encodedPassword))
        .withMessage(ErrorCode.PASSWORD_MISMATCH.getMessage());
  }

}