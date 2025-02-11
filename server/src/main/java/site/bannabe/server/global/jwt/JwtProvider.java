package site.bannabe.server.global.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Objects;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import site.bannabe.server.global.exceptions.BannabeAuthenticationException;
import site.bannabe.server.global.exceptions.ErrorCode;
import site.bannabe.server.global.utils.DateUtils;

@Component
@RequiredArgsConstructor
public class JwtProvider {

  private static final long ACCESS_TOKEN_EXPIRE_TIME = 30L; // 30분
  private static final long REFRESH_TOKEN_EXPIRE_TIME = 60L * 24 * 7; // 7일

  private static final String AUTHORITIES_KEY = "ROLE";

  @Value("${jwt.secret}")
  private String key;

  private SecretKey secretKey;
  private JwtParser parser;

  @PostConstruct
  public void init() {
    this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(key));
    this.parser = Jwts.parser().verifyWith(secretKey).build();
  }

  public GenerateToken generateToken(String entityToken, String authorities) {
    String accessToken = createToken(entityToken, authorities, ACCESS_TOKEN_EXPIRE_TIME);
    String refreshToken = createToken(entityToken, authorities, REFRESH_TOKEN_EXPIRE_TIME);
    return new GenerateToken(accessToken, refreshToken);
  }

  public void verifyToken(String token) {
    if (Objects.isNull(token)) {
      throw new BannabeAuthenticationException(ErrorCode.TOKEN_NOT_FOUND);
    }
    try {
      parser.parseSignedClaims(token);
    } catch (ExpiredJwtException e) {
      throw new BannabeAuthenticationException(ErrorCode.TOKEN_EXPIRED);
    } catch (Exception e) {
      throw new BannabeAuthenticationException(ErrorCode.INVALID_TOKEN);
    }
  }

  public String getEntityToken(String token) {
    try {
      return parser.parseSignedClaims(token).getPayload().getSubject();
    } catch (ExpiredJwtException e) {
      return e.getClaims().getSubject();
    } catch (Exception e) {
      throw new BannabeAuthenticationException(ErrorCode.INVALID_TOKEN);
    }
  }

  public String getAuthorities(String token) {
    return parser.parseSignedClaims(token).getPayload().get(AUTHORITIES_KEY, String.class);
  }

  public TokenClaims getTokenClaims(String token) {
    try {
      var claims = parser.parseSignedClaims(token).getPayload();
      return new TokenClaims(claims.getSubject(), claims.get(AUTHORITIES_KEY, String.class));
    } catch (ExpiredJwtException e) {
      throw new BannabeAuthenticationException(ErrorCode.TOKEN_EXPIRED);
    } catch (Exception e) {
      throw new BannabeAuthenticationException(ErrorCode.INVALID_TOKEN);
    }
  }

  private String createToken(String subject, String authorities, long expireTime) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime expiredDate = now.plusMinutes(expireTime);

    return Jwts.builder()
               .subject(subject)
               .claim(AUTHORITIES_KEY, authorities)
               .issuedAt(DateUtils.localDateTimeToDate(now))
               .expiration(DateUtils.localDateTimeToDate(expiredDate))
               .signWith(secretKey)
               .compact();
  }

  public record TokenClaims(
      String entityToken,
      String authorities
  ) {

  }

}