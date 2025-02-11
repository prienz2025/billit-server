package site.bannabe.server.domain.users.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.bannabe.server.domain.users.controller.request.AuthResetPasswordRequest;
import site.bannabe.server.domain.users.controller.request.AuthSendCodeRequest;
import site.bannabe.server.domain.users.controller.request.AuthVerifyCodeRequest;
import site.bannabe.server.domain.users.controller.request.TokenRefreshRequest;
import site.bannabe.server.domain.users.controller.request.UserRegisterRequest;
import site.bannabe.server.domain.users.service.AuthService;
import site.bannabe.server.global.type.TokenResponse;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public void registerUser(@RequestBody @Valid UserRegisterRequest registerRequest) {
    authService.registerUser(registerRequest);
  }

  @PostMapping("/token/refresh")
  public TokenResponse refreshToken(@RequestBody TokenRefreshRequest tokenRefreshRequest) {
    return authService.refreshToken(tokenRefreshRequest.refreshToken());
  }

  @PostMapping("/send-code")
  public void sendAuthCode(@RequestBody AuthSendCodeRequest authSendCodeRequest) {
    authService.sendAuthCode(authSendCodeRequest.email());
  }

  @PostMapping("/verify-code")
  public void verifyAuthCode(@RequestBody AuthVerifyCodeRequest authVerifyCodeRequest) {
    authService.verifyAuthCode(authVerifyCodeRequest.email(), authVerifyCodeRequest.authCode());
  }

  @PutMapping("/reset-password")
  public void changePassword(@RequestBody AuthResetPasswordRequest resetPasswordRequest) {
    authService.resetPassword(resetPasswordRequest);
  }

}