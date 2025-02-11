package site.bannabe.server.domain.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.bannabe.server.global.redis.AuthCodeClient;
import site.bannabe.server.global.type.AuthCode;

@Service
@RequiredArgsConstructor
public class AuthCodeService {

  private final AuthCodeClient authCodeClient;

  public void saveAuthCode(String key, String value) {
    AuthCode authCode = new AuthCode(value, false);
    authCodeClient.save(key, authCode);
  }

  public void markAuthCodeAsVerified(String key) {
    AuthCode authCode = authCodeClient.findBy(key);
    authCode.markAsVerified();
    long ttl = authCodeClient.getTTL(key);
    authCodeClient.updateAuthCodeWithTTL(key, authCode, ttl);
  }

  public AuthCode findAuthCode(String key) {
    return authCodeClient.findBy(key);
  }

  public void removeAuthCode(String key) {
    authCodeClient.deleteBy(key);
  }

}