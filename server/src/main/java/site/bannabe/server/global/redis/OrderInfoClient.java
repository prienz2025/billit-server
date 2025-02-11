package site.bannabe.server.global.redis;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;
import site.bannabe.server.global.type.OrderInfo;

@Component
@RequiredArgsConstructor
public class OrderInfoClient implements RedisValueClient<OrderInfo> {

  private static final long TTL = 5L;
  private final RedisTemplate<String, OrderInfo> redis;

  @Override
  public void save(String key, OrderInfo value) {
    key = generateKey(ORDER_INFO_FORMAT, key);
    redis.opsForValue().set(key, value, Duration.ofMinutes(TTL));
  }

  @Override
  public OrderInfo findBy(String key) {
    OrderInfo orderInfo = redis.opsForValue().get(generateKey(ORDER_INFO_FORMAT, key));
    return Optional.ofNullable(orderInfo).orElseThrow(() -> new BannabeServiceException(ErrorCode.ORDER_INFO_NOT_FOUND));
  }

  @Override
  public void deleteBy(String key) {
    redis.delete(generateKey(ORDER_INFO_FORMAT, key));
  }

  public boolean existByRentalToken(String rentalItemToken) {
    ScanOptions options = ScanOptions.scanOptions().match(ORDER_INFO_FORMAT.concat("*")).count(100).build();
    try (var cursor = redis.scan(options)) {
      while (cursor.hasNext()) {
        String key = cursor.next();
        OrderInfo orderInfo = redis.opsForValue().get(key);
        if (Objects.isNull(orderInfo)) {
          continue;
        }
        if (orderInfo.getRentalItemToken().equals(rentalItemToken)) {
          return true;
        }
      }
    }
    return false;
  }

}