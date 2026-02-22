package com.hms.api.dto;

public class AdminRoomResponse {
  private final String roomCode;
  private final String roomType;
  private final String bedType;
  private final int pricePerNight;
  private final int occupancyAdults;
  private final int occupancyChildren;
  private final int maxOccupancy;
  private final String amenitiesCsv;
  private final String availabilityStatus;
  private final String roomStatus;
  private final String description;
  private final boolean active;

  public AdminRoomResponse(
      String roomCode,
      String roomType,
      String bedType,
      int pricePerNight,
      int occupancyAdults,
      int occupancyChildren,
      int maxOccupancy,
      String amenitiesCsv,
      String availabilityStatus,
      String roomStatus,
      String description,
      boolean active
  ) {
    this.roomCode = roomCode;
    this.roomType = roomType;
    this.bedType = bedType;
    this.pricePerNight = pricePerNight;
    this.occupancyAdults = occupancyAdults;
    this.occupancyChildren = occupancyChildren;
    this.maxOccupancy = maxOccupancy;
    this.amenitiesCsv = amenitiesCsv;
    this.availabilityStatus = availabilityStatus;
    this.roomStatus = roomStatus;
    this.description = description;
    this.active = active;
  }

  public String getRoomCode() {
    return roomCode;
  }

  public String getRoomType() {
    return roomType;
  }

  public String getBedType() {
    return bedType;
  }

  public int getPricePerNight() {
    return pricePerNight;
  }

  public int getOccupancyAdults() {
    return occupancyAdults;
  }

  public int getOccupancyChildren() {
    return occupancyChildren;
  }

  public int getMaxOccupancy() {
    return maxOccupancy;
  }

  public String getAmenitiesCsv() {
    return amenitiesCsv;
  }

  public String getAvailabilityStatus() {
    return availabilityStatus;
  }

  public String getRoomStatus() {
    return roomStatus;
  }

  public String getDescription() {
    return description;
  }

  public boolean isActive() {
    return active;
  }
}
