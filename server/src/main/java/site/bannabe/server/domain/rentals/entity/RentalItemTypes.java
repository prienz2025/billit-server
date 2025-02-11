package site.bannabe.server.domain.rentals.entity;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.bannabe.server.global.type.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RentalItemTypes extends BaseEntity {

  private RentalItemCategory category;

  private String name;

  private String image;

  private String description;

  private Integer price;

  @Builder
  private RentalItemTypes(RentalItemCategory category, String name, String image, String description, Integer price) {
    this.category = category;
    this.name = name;
    this.image = image;
    this.description = description;
    this.price = price;
  }

}