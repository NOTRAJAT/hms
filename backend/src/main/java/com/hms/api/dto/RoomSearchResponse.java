package com.hms.api.dto;

import java.util.List;

public class RoomSearchResponse {
  private String roomId;
  private String roomType;
  private int price;
  private int occupancyAdults;
  private int occupancyChildren;
  private List<String> amenities;
  private int roomSizeSqFt;
  private String imageUrl;
  private boolean available;

  public RoomSearchResponse(
      String roomId,
      String roomType,
      int price,
      int occupancyAdults,
      int occupancyChildren,
      List<String> amenities,
      int roomSizeSqFt,
      String imageUrl,
      boolean available
  ) {
    this.roomId = roomId;
    this.roomType = roomType;
    this.price = price;
    this.occupancyAdults = occupancyAdults;
    this.occupancyChildren = occupancyChildren;
    this.amenities = amenities;
    this.roomSizeSqFt = roomSizeSqFt;
    this.imageUrl = imageUrl;
    this.available = available;
  }

  public String getRoomId() {
    return roomId;
  }

  public String getRoomType() {
    return roomType;
  }

  public int getPrice() {
    return price;
  }

  public int getOccupancyAdults() {
    return occupancyAdults;
  }

  public int getOccupancyChildren() {
    return occupancyChildren;
  }

  public List<String> getAmenities() {
    return amenities;
  }

  public int getRoomSizeSqFt() {
    return roomSizeSqFt;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public boolean isAvailable() {
    return available;
  }
}
