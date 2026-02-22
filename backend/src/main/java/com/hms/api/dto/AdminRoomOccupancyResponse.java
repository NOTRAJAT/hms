package com.hms.api.dto;

import java.util.List;

public class AdminRoomOccupancyResponse {
  private final String roomCode;
  private final List<AdminRoomOccupancyPoint> points;

  public AdminRoomOccupancyResponse(String roomCode, List<AdminRoomOccupancyPoint> points) {
    this.roomCode = roomCode;
    this.points = points;
  }

  public String getRoomCode() {
    return roomCode;
  }

  public List<AdminRoomOccupancyPoint> getPoints() {
    return points;
  }
}
