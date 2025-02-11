package site.bannabe.server.global.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;

@Component
@RequiredArgsConstructor
public class JsonUtils {

  private final ObjectMapper objectMapper;

  public String serializedObjectToJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new BannabeServiceException(ErrorCode.JSON_SERIALIZATION_ERROR);
    }
  }

  public <T> T deserializedJsonToObject(InputStream inputStream, Class<T> clazz) {
    try {
      return objectMapper.readValue(inputStream, clazz);
    } catch (IOException e) {
      throw new BannabeServiceException(ErrorCode.JSON_DESERIALIZATION_ERROR);
    }
  }

}