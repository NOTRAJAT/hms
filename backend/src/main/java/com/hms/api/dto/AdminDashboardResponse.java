package com.hms.api.dto;

public class AdminDashboardResponse {
  private final long dailyBookings;
  private final long weeklyBookings;
  private final long monthlyBookings;
  private final long availableRooms;

  public AdminDashboardResponse(long dailyBookings, long weeklyBookings, long monthlyBookings, long availableRooms) {
    this.dailyBookings = dailyBookings;
    this.weeklyBookings = weeklyBookings;
    this.monthlyBookings = monthlyBookings;
    this.availableRooms = availableRooms;
  }

  public long getDailyBookings() {
    return dailyBookings;
  }

  public long getWeeklyBookings() {
    return weeklyBookings;
  }

  public long getMonthlyBookings() {
    return monthlyBookings;
  }

  public long getAvailableRooms() {
    return availableRooms;
  }
}
