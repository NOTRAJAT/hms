package com.hms.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;

public class AdminRoomUpdateRequest {
  @NotBlank
  private String roomType;

  @NotBlank
  private String bedType;

  @NotNull
  @Min(1000)
  private Integer pricePerNight;

  @NotBlank
  private String roomStatus;

  @NotBlank
  private String amenitiesCsv;

  @NotNull
  @Min(1)
  @Max(10)
  private Integer occupancyAdults;

  @NotNull
  @Min(0)
  @Max(5)
  private Integer occupancyChildren;

  public String getRoomType() {
    return roomType;
  }

  public void setRoomType(String roomType) {
    this.roomType = roomType;
  }

  public String getBedType() {
    return bedType;
  }

  public void setBedType(String bedType) {
    this.bedType = bedType;
  }

  public Integer getPricePerNight() {
    return pricePerNight;
  }

  public void setPricePerNight(Integer pricePerNight) {
    this.pricePerNight = pricePerNight;
  }

  public String getRoomStatus() {
    return roomStatus;
  }

  public void setRoomStatus(String roomStatus) {
    this.roomStatus = roomStatus;
  }

  public String getAmenitiesCsv() {
    return amenitiesCsv;
  }

  public void setAmenitiesCsv(String amenitiesCsv) {
    this.amenitiesCsv = amenitiesCsv;
  }

  public Integer getOccupancyAdults() {
    return occupancyAdults;
  }

  public void setOccupancyAdults(Integer occupancyAdults) {
    this.occupancyAdults = occupancyAdults;
  }

  public Integer getOccupancyChildren() {
    return occupancyChildren;
  }

  public void setOccupancyChildren(Integer occupancyChildren) {
    this.occupancyChildren = occupancyChildren;
  }
}
