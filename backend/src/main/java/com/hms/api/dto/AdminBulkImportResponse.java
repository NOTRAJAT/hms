package com.hms.api.dto;

public class AdminBulkImportResponse {
  private final int importedCount;
  private final String message;

  public AdminBulkImportResponse(int importedCount, String message) {
    this.importedCount = importedCount;
    this.message = message;
  }

  public int getImportedCount() {
    return importedCount;
  }

  public String getMessage() {
    return message;
  }
}
