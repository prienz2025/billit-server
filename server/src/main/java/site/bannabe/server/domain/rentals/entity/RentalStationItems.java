package site.bannabe.server.domain.rentals.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;
import site.bannabe.server.global.type.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RentalStationItems extends BaseEntity {

  private Integer stock;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rental_station_id")
  private RentalStations rentalStation;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rental_item_type_id")
  private RentalItemTypes rentalItemType;

  public RentalStationItems(RentalItemTypes rentalItemType, RentalStations rentalStation, Integer stock) {
    this.rentalItemType = rentalItemType;
    this.rentalStation = rentalStation;
    this.stock = stock;
  }

  public void decreaseStock() {
    if (this.stock == 0) {
      throw new BannabeServiceException(ErrorCode.RENTAL_ITEM_STOCK_EMPTY);
    }
    this.stock--;
  }

  public void increaseStock() {
    this.stock++;
  }

}