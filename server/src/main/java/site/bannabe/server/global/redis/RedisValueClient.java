package site.bannabe.server.global.redis;

public interface RedisValueClient<T> extends RedisClient {

  void save(String key, T value);

  T findBy(String key);

  void deleteBy(String key);

}