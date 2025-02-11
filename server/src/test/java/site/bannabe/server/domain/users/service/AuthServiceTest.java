package site.bannabe.server.domain.users.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.bannabe.server.domain.users.controller.request.AuthResetPasswordRequest;
import site.bannabe.server.domain.users.controller.request.UserRegisterRequest;
import site.bannabe.server.domain.users.entity.Users;
import site.bannabe.server.domain.users.repository.UserRepository;
import site.bannabe.server.global.exceptions.BannabeAuthenticationException;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;
import site.bannabe.server.global.jwt.GenerateToken;
import site.bannabe.server.global.jwt.JwtService;
import site.bannabe.server.global.mail.MailService;
import site.bannabe.server.global.type.AuthCode;
import site.bannabe.server.global.type.TokenResponse;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private JwtService jwtService;

  @Mock
  private MailService mailService;

  @Mock
  private AuthCodeService authCodeService;

  @Mock
  private PasswordService passwordService;

  @InjectMocks
  private AuthService authService;

  private String email;
  private String authCode;
  private String newPassword;
  private String newPasswordConfirm;
  private String password;
  private String encodedPassword;
  private String refreshToken;
  private Users user;
  private AuthResetPasswordRequest request;

  @BeforeEach
  void setUp() {
    email = "test@test.com";
    authCode = "authCode";
    newPassword = "newPassword";
    newPasswordConfirm = "newPassword";
    password = "1234567890";
    encodedPassword = "encodedPassword";
    user = Users.builder().email(email).password(password).build();
    request = new AuthResetPasswordRequest(authCode, email, newPassword, newPasswordConfirm);
    refreshToken = "refreshToken";
  }

  @Test
  @DisplayName("회원가입 성공")
  void registerUser() {
    //given
    UserRegisterRequest registerRequest = new UserRegisterRequest(email, password);
    given(userRepository.existsByEmail(anyString())).willReturn(false);
    given(passwordService.encodePassword(anyString())).willReturn(encodedPassword);
    given(userRepository.save(any(Users.class))).willReturn(Users.builder().build());

    //when
    authService.registerUser(registerRequest);

    //then
    verify(userRepository).save(any(Users.class));
    verify(passwordService).encodePassword(anyString());
    verify(userRepository).save(any(Users.class));
  }

  @Test
  @DisplayName("이메일 중복시 예외 발생")
  void registerUserDuplicateEmail() {
    //given
    UserRegisterRequest registerRequest = new UserRegisterRequest(email, password);
    given(userRepository.existsByEmail(anyString())).willReturn(true);

    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> authService.registerUser(registerRequest))
        .withMessage(ErrorCode.DUPLICATE_EMAIL.getMessage());

    verify(passwordService, never()).encodePassword(anyString());
    verify(userRepository, never()).save(any(Users.class));
  }

  @Test
  @DisplayName("JWT 갱신 성공")
  void refreshToken() {
    //given
    String newAccessToken = "newAccessToken";
    String newRefreshToken = "newRefreshToken";
    given(jwtService.refreshJWT(refreshToken)).willReturn(new GenerateToken(newAccessToken, newRefreshToken));

    //when
    TokenResponse tokenResponse = authService.refreshToken(refreshToken);

    //then
    assertThat(tokenResponse).isNotNull()
                             .extracting(TokenResponse::accessToken, TokenResponse::refreshToken)
                             .containsExactly(newAccessToken, newRefreshToken);

    verify(jwtService).validateToken(refreshToken);
  }

  @Test
  @DisplayName("토큰 검증 실패시 예외 발생")
  void validateTokenTest() {
    //given
    willThrow(new BannabeAuthenticationException(ErrorCode.INVALID_TOKEN)).given(jwtService).validateToken(anyString());

    //when then
    assertThatExceptionOfType(BannabeAuthenticationException.class)
        .isThrownBy(() -> authService.refreshToken(refreshToken))
        .withMessage(ErrorCode.INVALID_TOKEN.getMessage());

    verify(jwtService, never()).refreshJWT(anyString());
  }

  @Test
  @DisplayName("인증코드 전송")
  void sendAuthCode() {
    //given
    given(userRepository.existsByEmail(email)).willReturn(true);

    //when
    authService.sendAuthCode(email);

    //then
    verify(userRepository).existsByEmail(email);
    verify(authCodeService).saveAuthCode(eq(email), anyString());
    verify(mailService).sendAuthCodeMail(eq(email), anyString());
  }

  @Test
  @DisplayName("메일이 존재하지 않을 시 예외 발생")
  void sendAuthCodeDuplicateEmail() {
    //given
    given(userRepository.existsByEmail(email)).willReturn(false);

    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> authService.sendAuthCode(email))
        .withMessage(ErrorCode.USER_NOT_FOUND.getMessage());

    verify(authCodeService, never()).saveAuthCode(eq(email), anyString());
    verify(mailService, never()).sendAuthCodeMail(eq(email), anyString());
  }

  @Test
  @DisplayName("인증코드 검증 성공")
  void verifyAuthCode() {
    //given
    given(authCodeService.findAuthCode(email)).willReturn(new AuthCode(authCode, false));

    //when
    authService.verifyAuthCode(email, authCode);

    //then
    verify(authCodeService).markAuthCodeAsVerified(email);
  }

  @Test
  @DisplayName("이미 인증된 코드라면 예외 발생")
  void alreadyVerifiedAuthCode() {
    //given
    given(authCodeService.findAuthCode(email)).willReturn(new AuthCode(authCode, true));

    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> authService.verifyAuthCode(email, authCode))
        .withMessage(ErrorCode.AUTH_CODE_ALREADY_VERIFIED.getMessage());

    verify(authCodeService, never()).markAuthCodeAsVerified(email);
  }

  @Test
  @DisplayName("인증코드 검증 시 인증코드 불일치시 예외 발생")
  void mismatchAuthCode() {
    //given
    given(authCodeService.findAuthCode(email)).willReturn(new AuthCode("wrongAuthCode", false));

    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> authService.verifyAuthCode(email, authCode))
        .withMessage(ErrorCode.AUTH_CODE_MISMATCH.getMessage());

    verify(authCodeService, never()).markAuthCodeAsVerified(email);
  }

  // 비밀번호 재설정 테스트
  @Test
  @DisplayName("비밀번호 재설정 성공")
  void resetPassword() {
    //given
    given(authCodeService.findAuthCode(email)).willReturn(new AuthCode(authCode, true));
    given(userRepository.findByEmail(email)).willReturn(user);
    given(passwordService.encodePassword(newPassword)).willReturn(encodedPassword);

    //when
    authService.resetPassword(request);

    //then
    assertThat(user.getPassword()).isEqualTo(encodedPassword);

    verify(authCodeService).findAuthCode(email);
    verify(passwordService).validateNewPassword(anyString(), anyString());
    verify(userRepository).findByEmail(email);
    verify(passwordService).validateReusedPassword(newPassword, password);
    verify(passwordService).encodePassword(newPassword);
    verify(authCodeService).removeAuthCode(email);
  }

  @Test
  @DisplayName("미검증 인증코드 예외 발생")
  void resetPasswordNotVerifiedAuthCode() {
    //given
    given(authCodeService.findAuthCode(email)).willReturn(new AuthCode(authCode, false));

    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> authService.resetPassword(request))
        .withMessage(ErrorCode.AUTH_CODE_NOT_VERIFIED.getMessage());

    verify(authCodeService).findAuthCode(email);
    verify(passwordService, never()).validateNewPassword(newPassword, newPasswordConfirm);
    verify(userRepository, never()).findByEmail(email);
    verify(passwordService, never()).validateReusedPassword(eq(newPassword), anyString());
    verify(passwordService, never()).encodePassword(newPassword);
    verify(authCodeService, never()).removeAuthCode(email);
  }

  @Test
  @DisplayName("비밀번호 재설정 중 인증코드 불일치 시 예외 발생")
  void resetPasswordMismatchAuthCode() {
    //given
    given(authCodeService.findAuthCode(email)).willReturn(new AuthCode("wrongAuthCode", true));

    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> authService.resetPassword(request))
        .withMessage(ErrorCode.AUTH_CODE_MISMATCH.getMessage());

    verify(authCodeService).findAuthCode(email);
    verify(passwordService, never()).validateNewPassword(newPassword, newPasswordConfirm);
    verify(userRepository, never()).findByEmail(email);
    verify(passwordService, never()).validateReusedPassword(eq(newPassword), anyString());
    verify(passwordService, never()).encodePassword(newPassword);
    verify(authCodeService, never()).removeAuthCode(email);
  }

  @Test
  @DisplayName("새 비밀번호 불일치 시 예외 발생")
  void resetPasswordValidateNewPassword() {
    //given
    given(authCodeService.findAuthCode(email)).willReturn(new AuthCode(authCode, true));
    willThrow(new BannabeServiceException(ErrorCode.NEW_PASSWORD_MISMATCH)).given(passwordService)
                                                                           .validateNewPassword(newPassword, newPasswordConfirm);
    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> authService.resetPassword(request))
        .withMessage(ErrorCode.NEW_PASSWORD_MISMATCH.getMessage());

    verify(authCodeService).findAuthCode(email);
    verify(passwordService).validateNewPassword(newPassword, newPasswordConfirm);
    verify(userRepository, never()).findByEmail(email);
    verify(passwordService, never()).validateReusedPassword(eq(newPassword), anyString());
    verify(passwordService, never()).encodePassword(newPassword);
    verify(authCodeService, never()).removeAuthCode(email);
  }

  @Test
  @DisplayName("비밀번호 재사용 시 예외 발생")
  void reusedPassword() {
    //given
    given(authCodeService.findAuthCode(email)).willReturn(new AuthCode(authCode, true));
    given(userRepository.findByEmail(email)).willReturn(user);
    willThrow(new BannabeServiceException(ErrorCode.DUPLICATE_PASSWORD)).given(passwordService)
                                                                        .validateReusedPassword(newPassword, user.getPassword());
    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> authService.resetPassword(request))
        .withMessage(ErrorCode.DUPLICATE_PASSWORD.getMessage());

    verify(authCodeService).findAuthCode(email);
    verify(passwordService).validateNewPassword(newPassword, newPasswordConfirm);
    verify(userRepository).findByEmail(email);
    verify(passwordService).validateReusedPassword(eq(newPassword), anyString());
    verify(passwordService, never()).encodePassword(newPassword);
    verify(authCodeService, never()).removeAuthCode(email);
  }

}