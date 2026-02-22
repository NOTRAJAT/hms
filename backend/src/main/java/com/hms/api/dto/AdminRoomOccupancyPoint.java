package com.hms.api.dto;

public class AdminRoomOccupancyPoint {
  private final String date;
  private final boolean occupied;

  public AdminRoomOccupancyPoint(String date, boolean occupied) {
    this.date = date;
    this.occupied = occupied;
  }

  public String getDate() {
    return date;
  }

  public boolean isOccupied() {
    return occupied;
  }
}
