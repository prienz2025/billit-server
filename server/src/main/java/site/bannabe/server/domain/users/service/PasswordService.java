package site.bannabe.server.domain.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;

@Service
@RequiredArgsConstructor
public class PasswordService {

  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  public String encodePassword(String password) {
    return bCryptPasswordEncoder.encode(password);
  }

  public void validateNewPassword(String newPassword, String newPasswordConfirm) {
    if (!newPassword.equals(newPasswordConfirm)) {
      throw new BannabeServiceException(ErrorCode.NEW_PASSWORD_MISMATCH);
    }
  }

  public void validateReusedPassword(String newPassword, String currentPassword) {
    if (bCryptPasswordEncoder.matches(newPassword, currentPassword)) {
      throw new BannabeServiceException(ErrorCode.DUPLICATE_PASSWORD);
    }
  }

  public void validateCurrentPassword(String currentPassword, String encodedPassword) {
    if (!bCryptPasswordEncoder.matches(currentPassword, encodedPassword)) {
      throw new BannabeServiceException(ErrorCode.PASSWORD_MISMATCH);
    }
  }

}