package site.bannabe.server.global.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import site.bannabe.server.global.exceptions.BannabeAuthenticationException;
import site.bannabe.server.global.exceptions.ErrorCode;
import site.bannabe.server.global.jwt.JwtProvider.TokenClaims;
import site.bannabe.server.global.security.auth.PrincipalDetails;
import site.bannabe.server.global.type.UserTokens;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

  @InjectMocks
  private JwtService jwtService;

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private UserTokenService userTokenService;

  @Test
  @DisplayName("JWT 정상 생성")
  void createJWT() {
    //given
    String entityToken = "entityToken";
    String authorities = "ROLE_USER";
    String deviceToken = "deviceToken";
    GenerateToken generateToken = new GenerateToken("accessToken", "refreshToken");

    given(jwtProvider.generateToken(entityToken, authorities)).willReturn(generateToken);

    //when
    GenerateToken result = jwtService.createJWT(entityToken, authorities, deviceToken);

    //then
    assertThat(result).isEqualTo(generateToken);
    verify(userTokenService).save(entityToken, generateToken.refreshToken(), deviceToken);
  }

  @Test
  @DisplayName("토큰 재발급 정상 동작")
  void refreshJWT() {
    //given
    String refreshToken = "refreshToken";
    String entityToken = "entityToken";
    String authorities = "ROLE_USER";
    TokenClaims tokenClaims = new TokenClaims(entityToken, authorities);
    String deviceToken = "deviceToken";
    UserTokens userTokens = new UserTokens(refreshToken, deviceToken);
    GenerateToken generateToken = new GenerateToken("newAccessToken", "newRefreshToken");

    given(jwtProvider.getTokenClaims(refreshToken)).willReturn(tokenClaims);
    given(userTokenService.findBy(entityToken, refreshToken)).willReturn(userTokens);
    given(jwtProvider.generateToken(entityToken, authorities)).willReturn(generateToken);

    //when
    GenerateToken result = jwtService.refreshJWT(refreshToken);

    //then
    assertThat(result).isEqualTo(generateToken);
    verify(jwtProvider).getTokenClaims(refreshToken);
    verify(jwtProvider).generateToken(entityToken, authorities);
    verify(userTokenService).updateUserToken(entityToken, generateToken.refreshToken(), deviceToken);
  }

  @Test
  @DisplayName("RefreshToken 불일치 시 예외 발생")
  void mismatchRefreshToken() {
    //given
    String refreshToken = "refreshToken";
    String savedRefreshToken = "savedRefreshToken";
    String entityToken = "entityToken";
    TokenClaims tokenClaims = new TokenClaims(entityToken, "ROLE_USER");
    UserTokens userTokens = new UserTokens(savedRefreshToken, "deviceToken");
    given(jwtProvider.getTokenClaims(refreshToken)).willReturn(tokenClaims);
    given(userTokenService.findBy(entityToken, refreshToken)).willReturn(userTokens);

    //when then
    assertThatExceptionOfType(BannabeAuthenticationException.class)
        .isThrownBy(() -> jwtService.refreshJWT(refreshToken))
        .withMessage(ErrorCode.INVALID_REFRESH_TOKEN.getMessage());

    verify(jwtProvider, never()).generateToken(tokenClaims.entityToken(), tokenClaims.authorities());
    verify(userTokenService, never()).updateUserToken(eq(tokenClaims.entityToken()), anyString(),
        eq(userTokens.getDeviceToken()));
  }

  @Test
  @DisplayName("saveAuthentication 정상 동작")
  void saveAuthentication() {
    //given
    String accessToken = "accessToken";
    String entityToken = "entityToken";
    String authorities = "ROLE_USER";
    List<SimpleGrantedAuthority> expectedAuthorities = List.of(new SimpleGrantedAuthority(authorities));

    given(jwtProvider.getEntityToken(accessToken)).willReturn(entityToken);
    given(jwtProvider.getAuthorities(accessToken)).willReturn(authorities);

    //when
    jwtService.saveAuthentication(accessToken);

    //then
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNotNull();
    assertThat(authentication.getPrincipal()).isInstanceOf(PrincipalDetails.class);
    assertThat(authentication.getAuthorities()).isEqualTo(expectedAuthorities);
  }

  @Test
  @DisplayName("refreshToken 정상 삭제")
  void removeRefreshToken() {
    //given
    String accessToken = "accessToken";
    String refreshToken = "refreshToken";
    String entityToken = "entityToken";

    given(jwtProvider.getEntityToken(accessToken)).willReturn(entityToken);

    //when
    jwtService.removeRefreshToken(accessToken, refreshToken);

    //then
    verify(userTokenService).removeUserToken(entityToken, refreshToken);
  }

}