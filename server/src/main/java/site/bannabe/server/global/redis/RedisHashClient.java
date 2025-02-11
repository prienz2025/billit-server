package site.bannabe.server.global.redis;

public interface RedisHashClient<T> extends RedisClient {

  void save(String key, T value);

  T findBy(String key, String hashKey);

  void deleteBy(String key, String hashKey);

  void deleteAll(String key);

}