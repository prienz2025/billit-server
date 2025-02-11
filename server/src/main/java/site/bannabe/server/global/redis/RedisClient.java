package site.bannabe.server.global.redis;

public interface RedisClient {

  String USER_TOKEN_FORMAT = "UserToken:%s";
  String AUTH_CODE_FORMAT = "AuthCode:%s";
  String ORDER_INFO_FORMAT = "OrderInfo:%s";

  default String generateKey(String prefix, String key) {
    return String.format(prefix, key);
  }

}