package io.billit.server.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import io.billit.server.global.redis.AuthCodeClient;
import io.billit.server.global.redis.UserTokenClient;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@DataRedisTest
@Import({TestRedisConfig.class, UserTokenClient.class, AuthCodeClient.class})
public @interface RedisTest {

}
