package site.bannabe.server.domain.rentals.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.bannabe.server.global.type.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RentalStations extends BaseEntity {

  private String name;

  private String address;

  @Column(precision = 9, scale = 6)
  private BigDecimal latitude;

  @Column(precision = 9, scale = 6)
  private BigDecimal longitude;

  private String openTime;

  private String closeTime;

  private String closeDay; // 휴무일 (문자열로 저장)

  private StationStatus status;

  private StationGrade grade;

  @Builder
  private RentalStations(String name, String address, BigDecimal latitude, BigDecimal longitude, String openTime,
      String closeTime,
      String closeDay, StationStatus status, StationGrade grade) {
    this.name = name;
    this.address = address;
    this.latitude = latitude;
    this.longitude = longitude;
    this.openTime = openTime;
    this.closeTime = closeTime;
    this.closeDay = closeDay;
    this.status = status;
    this.grade = grade;
  }

}