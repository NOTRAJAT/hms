package com.hms.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ComplaintRequest {
  @NotBlank
  private String userId;

  @NotBlank
  private String category;

  @NotBlank
  private String bookingId;

  @NotBlank
  @Size(min = 10, max = 100)
  private String title;

  @NotBlank
  @Size(min = 20, max = 500)
  private String description;

  @NotBlank
  private String contactPreference;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getBookingId() {
    return bookingId;
  }

  public void setBookingId(String bookingId) {
    this.bookingId = bookingId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getContactPreference() {
    return contactPreference;
  }

  public void setContactPreference(String contactPreference) {
    this.contactPreference = contactPreference;
  }
}
