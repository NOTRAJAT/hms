package com.hms.api;

public class ApiError {
  private String field;
  private String error;

  public ApiError(String field, String error) {
    this.field = field;
    this.error = error;
  }

  public ApiError(String error) {
    this.error = error;
  }

  public String getField() {
    return field;
  }

  public String getError() {
    return error;
  }
}
