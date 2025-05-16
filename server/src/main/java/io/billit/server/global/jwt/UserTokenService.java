package io.billit.server.global.jwt;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import io.billit.server.global.redis.UserTokenClient;
import io.billit.server.global.type.UserTokens;

@Service
@RequiredArgsConstructor
public class UserTokenService {

  private final UserTokenClient userTokenClient;

  public void save(String key, String refreshToken, String deviceToken) {
    UserTokens userTokens = new UserTokens(refreshToken, deviceToken);
    userTokenClient.save(key, userTokens);
  }

  public void updateUserToken(String key, String refreshToken, String deviceToken) {
    UserTokens userTokens = new UserTokens(refreshToken, deviceToken);
    userTokenClient.save(key, userTokens);
  }

  public UserTokens findBy(String key, String refreshToken) {
    return userTokenClient.findBy(key, refreshToken);
  }

  public List<UserTokens> findAllUserTokens(String key) {
    return userTokenClient.findAll(key);
  }

  public void removeUserToken(String key, String refreshToken) {
    userTokenClient.deleteBy(key, refreshToken);
  }

  public void removeAllUserToken(String key) {
    userTokenClient.deleteAll(key);
  }

}