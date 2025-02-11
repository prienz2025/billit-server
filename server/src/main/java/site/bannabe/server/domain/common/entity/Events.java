package site.bannabe.server.domain.common.entity;

import jakarta.persistence.Entity;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.bannabe.server.global.type.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Events extends BaseEntity {

  private String title;

  private String bannerImage;

  private String contentImage;

  private LocalDateTime startDate;

  private LocalDateTime endDate;

  private EventStatus status;

}