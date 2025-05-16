package io.billit.server.domain.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.billit.server.domain.users.controller.request.AuthResetPasswordRequest;
import io.billit.server.domain.users.controller.request.UserRegisterRequest;
import io.billit.server.domain.users.entity.Users;
import io.billit.server.domain.users.repository.UserRepository;
import io.billit.server.global.exceptions.BannabeServiceException;
import io.billit.server.global.exceptions.ErrorCode;
import io.billit.server.global.jwt.GenerateToken;
import io.billit.server.global.jwt.JwtService;
import io.billit.server.global.mail.MailService;
import io.billit.server.global.type.AuthCode;
import io.billit.server.global.type.TokenResponse;
import io.billit.server.global.utils.RandomCodeGenerator;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final MailService mailService;
  private final AuthCodeService authCodeService;
  private final PasswordService passwordService;

  @Value("${bannabe.default-profile-image}")
  private String defaultProfileImage;

  @Transactional
  public void registerUser(UserRegisterRequest registerRequest) {
    Boolean isDuplicateEmail = userRepository.existsByEmail(registerRequest.email());
    if (isDuplicateEmail) {
      throw new BannabeServiceException(ErrorCode.DUPLICATE_EMAIL);
    }

    String encodedPassword = passwordService.encodePassword(registerRequest.password());

    Users user = Users.createUser(registerRequest.email(), encodedPassword, defaultProfileImage);

    userRepository.save(user);
  }

  public TokenResponse refreshToken(String refreshToken) {
    jwtService.validateToken(refreshToken);
    GenerateToken generateToken = jwtService.refreshJWT(refreshToken);
    return TokenResponse.create(generateToken);
  }

  @Transactional(readOnly = true)
  public void sendAuthCode(String email) {
    boolean isExistEmail = userRepository.existsByEmail(email);
    if (!isExistEmail) {
      throw new BannabeServiceException(ErrorCode.USER_NOT_FOUND);
    }
    String authCode = RandomCodeGenerator.generateAuthCode();

    authCodeService.saveAuthCode(email, authCode);

    mailService.sendAuthCodeMail(email, authCode);
  }

  public void verifyAuthCode(String email, String authCode) {
    AuthCode savedAuthCode = authCodeService.findAuthCode(email);

    if (savedAuthCode.isVerified()) {
      throw new BannabeServiceException(ErrorCode.AUTH_CODE_ALREADY_VERIFIED);
    }

    validateAuthCode(authCode, savedAuthCode.getAuthCode());

    authCodeService.markAuthCodeAsVerified(email);
  }

  @Transactional
  public void resetPassword(AuthResetPasswordRequest resetPasswordRequest) {
    String email = resetPasswordRequest.email();
    String newPassword = resetPasswordRequest.newPassword();
    String newPasswordConfirm = resetPasswordRequest.newPasswordConfirm();

    AuthCode savedAuthCode = authCodeService.findAuthCode(email);
    if (!savedAuthCode.isVerified()) {
      throw new BannabeServiceException(ErrorCode.AUTH_CODE_NOT_VERIFIED);
    }
    validateAuthCode(resetPasswordRequest.authCode(), savedAuthCode.getAuthCode());

    passwordService.validateNewPassword(newPassword, newPasswordConfirm);

    Users findUser = userRepository.findByEmail(email);

    passwordService.validateReusedPassword(newPassword, findUser.getPassword());

    String encodePassword = passwordService.encodePassword(newPassword);
    findUser.changePassword(encodePassword);
    authCodeService.removeAuthCode(email);
  }

  private void validateAuthCode(String authCode, String savedAuthCode) {
    if (!authCode.equals(savedAuthCode)) {
      throw new BannabeServiceException(ErrorCode.AUTH_CODE_MISMATCH);
    }
  }

}