package site.bannabe.server.global.jwt;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.bannabe.server.global.redis.UserTokenClient;
import site.bannabe.server.global.type.UserTokens;

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