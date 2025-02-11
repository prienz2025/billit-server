package site.bannabe.server.global.utils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import site.bannabe.server.domain.payments.entity.RentalPayments;
import site.bannabe.server.domain.rentals.entity.RentalHistory;
import site.bannabe.server.domain.rentals.entity.RentalItems;
import site.bannabe.server.domain.users.entity.Users;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;
import site.bannabe.server.global.type.BaseEntity;

public class RandomCodeGenerator {

  private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private static final int NICKNAME_LENGTH = 12;
  private static final int AUTH_CODE_LENGTH = 6;
  private static final int TOKEN_LENGTH = 10;
  private static final Map<Class<?>, Prefix> PREFIX_MAP = Map.of(
      Users.class, Prefix.USER,
      RentalItems.class, Prefix.RENTAL_ITEM,
      RentalHistory.class, Prefix.RENTAL_HISTORY,
      RentalPayments.class, Prefix.RENTAL_PAYMENT
  );

  private static final SecureRandom RANDOM = new SecureRandom();

  public static String generateRandomNickname() {
    return generateRandomString(Prefix.NICKNAME.value(), NICKNAME_LENGTH);
  }

  public static String generateAuthCode() {
    return generateRandomString(Prefix.NONE.value(), AUTH_CODE_LENGTH);
  }

  public static <T extends BaseEntity> String generateRandomToken(Class<T> clazz) {
    Prefix prefix = PREFIX_MAP.get(clazz);
    if (prefix == null) {
      throw new BannabeServiceException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
    return generateRandomString(prefix.value(), TOKEN_LENGTH);
  }

  public static String generateOrderId(LocalDateTime now) {
    String prefix = Prefix.ORDER.value() + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
    return generateRandomString(prefix, 6);
  }

  private static String generateRandomString(String prefix, int length) {
    StringBuilder sb = new StringBuilder(prefix);
    for (int i = 0; i < length; i++) {
      sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
    }
    return sb.toString();
  }

  @RequiredArgsConstructor
  private enum Prefix {

    NONE(""),
    NICKNAME("Bannabe_"),
    USER("USR_"),
    RENTAL_ITEM("RI_"),
    RENTAL_HISTORY("RH_"),
    RENTAL_PAYMENT("RP_"),
    ORDER("ORD_");

    private final String value;

    public String value() {
      return value;
    }

  }

}