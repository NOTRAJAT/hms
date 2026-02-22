package com.hms.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AdminRoomCreateRequest {
  @NotBlank
  private String roomType;

  private String bedType;

  @NotNull
  @Min(1000)
  private Integer pricePerNight;

  private String amenitiesCsv;

  @NotBlank
  private String availability;

  @Min(1)
  @Max(10)
  private Integer maxOccupancy;

  @Min(1)
  @Max(10)
  private Integer occupancyAdults;

  @Min(0)
  @Max(5)
  private Integer occupancyChildren;

  @Size(max = 500)
  private String description;

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

  public String getAmenitiesCsv() {
    return amenitiesCsv;
  }

  public void setAmenitiesCsv(String amenitiesCsv) {
    this.amenitiesCsv = amenitiesCsv;
  }

  public String getAvailability() {
    return availability;
  }

  public void setAvailability(String availability) {
    this.availability = availability;
  }

  public Integer getMaxOccupancy() {
    return maxOccupancy;
  }

  public void setMaxOccupancy(Integer maxOccupancy) {
    this.maxOccupancy = maxOccupancy;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
