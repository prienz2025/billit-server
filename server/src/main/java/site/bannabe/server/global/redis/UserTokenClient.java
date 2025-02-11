package site.bannabe.server.global.redis;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;
import site.bannabe.server.global.type.UserTokens;

@Component
@RequiredArgsConstructor
public class UserTokenClient implements RedisHashClient<UserTokens> {

  private static final Long TTL = 7L;
  private final RedisTemplate<String, UserTokens> redis;

  @Override
  public void save(String key, UserTokens value) {
    key = generateKey(USER_TOKEN_FORMAT, key);
    String deviceToken = Objects.nonNull(value.getDeviceToken()) ? value.getDeviceToken() : ""; // 임시 코드. FCM 추가 이후 삭제
    redis.opsForHash().put(key, value.getRefreshToken(), deviceToken);
    redis.expire(key, Duration.ofDays(TTL));
  }

  @Override
  public UserTokens findBy(String key, String hashKey) {
    key = generateKey(USER_TOKEN_FORMAT, key);
    String deviceToken = (String) redis.opsForHash().get(key, hashKey);
    return Optional.ofNullable(deviceToken)
                   .map(token -> new UserTokens(hashKey, token))
                   .orElseThrow(() -> new BannabeServiceException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));
  }

  public List<UserTokens> findAll(String key) {
    key = generateKey(USER_TOKEN_FORMAT, key);
    Map<Object, Object> entries = redis.opsForHash().entries(key);

    if (entries.isEmpty()) {
      throw new BannabeServiceException(ErrorCode.USER_TOKEN_NOT_FOUND);
    }

    return entries.entrySet().stream()
                  .map(entry -> new UserTokens(String.valueOf(entry.getKey()), String.valueOf(entry.getValue())))
                  .toList();
  }

  @Override
  public void deleteBy(String key, String hashKey) {
    key = generateKey(USER_TOKEN_FORMAT, key);
    redis.opsForHash().delete(key, hashKey);
  }

  @Override
  public void deleteAll(String key) {
    key = generateKey(USER_TOKEN_FORMAT, key);
    redis.delete(key);
  }

}