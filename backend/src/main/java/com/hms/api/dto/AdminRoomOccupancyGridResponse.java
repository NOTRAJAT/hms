package com.hms.api.dto;

import java.util.List;

public class AdminRoomOccupancyGridResponse {
  private final String roomType;
  private final int page;
  private final int pageSize;
  private final int totalRooms;
  private final int totalPages;
  private final List<String> dates;
  private final List<Row> rows;

  public AdminRoomOccupancyGridResponse(
      String roomType,
      int page,
      int pageSize,
      int totalRooms,
      int totalPages,
      List<String> dates,
      List<Row> rows
  ) {
    this.roomType = roomType;
    this.page = page;
    this.pageSize = pageSize;
    this.totalRooms = totalRooms;
    this.totalPages = totalPages;
    this.dates = dates;
    this.rows = rows;
  }

  public String getRoomType() {
    return roomType;
  }

  public int getPage() {
    return page;
  }

  public int getPageSize() {
    return pageSize;
  }

  public int getTotalRooms() {
    return totalRooms;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public List<String> getDates() {
    return dates;
  }

  public List<Row> getRows() {
    return rows;
  }

  public static class Row {
    private final String roomCode;
    private final List<Boolean> occupied;

    public Row(String roomCode, List<Boolean> occupied) {
      this.roomCode = roomCode;
      this.occupied = occupied;
    }

    public String getRoomCode() {
      return roomCode;
    }

    public List<Boolean> getOccupied() {
      return occupied;
    }
  }
}
