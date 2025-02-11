package site.bannabe.server.domain.common.entity;

import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.bannabe.server.global.converter.AbstractEnumConverter;

@Getter
@RequiredArgsConstructor
public enum EventStatus {

  UP_COMING("예정"),
  ON_GOING("진행중"),
  END("종료");

  private final String description;

  @Converter(autoApply = true)
  static class EnumConverter extends AbstractEnumConverter<EventStatus> {

    public EnumConverter() {
      super(EventStatus.class);
    }

  }

}