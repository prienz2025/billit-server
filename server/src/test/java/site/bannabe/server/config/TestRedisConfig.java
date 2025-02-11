package site.bannabe.server.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@TestConfiguration
public class TestRedisConfig {

  @Bean
  public RedisTemplate<String, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, ?> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    redisTemplate.setKeySerializer(RedisSerializer.string());
    redisTemplate.setValueSerializer(RedisSerializer.json());
    redisTemplate.setHashKeySerializer(RedisSerializer.string());
    redisTemplate.setHashValueSerializer(RedisSerializer.json());
    redisTemplate.setEnableTransactionSupport(true);
    return redisTemplate;
  }

}