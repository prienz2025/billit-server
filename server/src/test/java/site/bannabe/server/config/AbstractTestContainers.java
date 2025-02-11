package site.bannabe.server.config;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
public abstract class AbstractTestContainers {

  @ServiceConnection(name = "mysql")
  static final MySQLContainer<?> mysqlContainer;

  @ServiceConnection(name = "redis")
  static final GenericContainer<?> redisContainer;

  static {
    mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.34"))
        .withDatabaseName("test");

    redisContainer = new RedisContainer(DockerImageName.parse("redis:latest"))
        .withExposedPorts(6379)
        .withCommand("redis-server", "--requirepass", "testpassword");

    mysqlContainer.start();
    redisContainer.start();

    System.setProperty("REDIS_HOST", redisContainer.getHost());
    System.setProperty("REDIS_PORT", String.valueOf(redisContainer.getFirstMappedPort()));
    System.setProperty("REDIS_PASSWORD", "testpassword");
  }

}