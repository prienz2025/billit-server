package site.bannabe.server.global.utils;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class DateUtils {

  public static Date localDateTimeToDate(LocalDateTime localDateTime) {
    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
  }

}