package site.bannabe.server.domain.users.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.bannabe.server.global.type.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Memberships extends BaseEntity {

  private Boolean status;

  private LocalDateTime startDate;

  private LocalDateTime expirationDate;

  private Integer extensionCount;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private Users user;

}