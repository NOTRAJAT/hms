package com.hms.api.dto;

import java.util.List;

public class AdminBookingPageResponse {
  private final List<AdminBookingResponse> items;
  private final int page;
  private final int size;
  private final long totalItems;
  private final int totalPages;

  public AdminBookingPageResponse(List<AdminBookingResponse> items, int page, int size, long totalItems, int totalPages) {
    this.items = items;
    this.page = page;
    this.size = size;
    this.totalItems = totalItems;
    this.totalPages = totalPages;
  }

  public List<AdminBookingResponse> getItems() {
    return items;
  }

  public int getPage() {
    return page;
  }

  public int getSize() {
    return size;
  }

  public long getTotalItems() {
    return totalItems;
  }

  public int getTotalPages() {
    return totalPages;
  }
}

