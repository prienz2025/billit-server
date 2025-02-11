package site.bannabe.server.global.jwt;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import site.bannabe.server.global.exceptions.BannabeAuthenticationException;
import site.bannabe.server.global.exceptions.ErrorCode;
import site.bannabe.server.global.jwt.JwtProvider.TokenClaims;
import site.bannabe.server.global.security.auth.PrincipalDetails;
import site.bannabe.server.global.type.UserTokens;

@Service
@RequiredArgsConstructor
public class JwtService {

  private final JwtProvider jwtProvider;

  private final UserTokenService userTokenService;

  public GenerateToken createJWT(String entityToken, String authorities, String deviceToken) {
    GenerateToken generateToken = jwtProvider.generateToken(entityToken, authorities);
    userTokenService.save(entityToken, generateToken.refreshToken(), deviceToken);
    return generateToken;
  }

  public GenerateToken refreshJWT(String requestRefreshToken) {
    TokenClaims tokenClaims = jwtProvider.getTokenClaims(requestRefreshToken);
    UserTokens userTokens = userTokenService.findBy(tokenClaims.entityToken(), requestRefreshToken);
    if (!requestRefreshToken.equals(userTokens.getRefreshToken())) {
      throw new BannabeAuthenticationException(ErrorCode.INVALID_REFRESH_TOKEN);
    }
    GenerateToken newToken = jwtProvider.generateToken(tokenClaims.entityToken(), tokenClaims.authorities());
    userTokenService.updateUserToken(tokenClaims.entityToken(), newToken.refreshToken(), userTokens.getDeviceToken());
    return newToken;
  }

  public void validateToken(String token) {
    jwtProvider.verifyToken(token);
  }

  public void saveAuthentication(String accessToken) {
    String entityToken = jwtProvider.getEntityToken(accessToken);
    String role = jwtProvider.getAuthorities(accessToken);
    List<SimpleGrantedAuthority> authorities = Arrays.stream(role.split(",")).map(SimpleGrantedAuthority::new).toList();
    PrincipalDetails user = PrincipalDetails.create(entityToken, authorities);
    Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  public void removeRefreshToken(String accessToken, String refreshToken) {
    String entityToken = jwtProvider.getEntityToken(accessToken);
    userTokenService.removeUserToken(entityToken, refreshToken);
  }

}