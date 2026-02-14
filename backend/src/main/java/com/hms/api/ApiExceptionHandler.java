package com.hms.api;

import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
  private static final Map<String, String> FIELD_MESSAGES = Map.ofEntries(
      Map.entry("name", "Name must be at least 3 characters long and contain only letters."),
      Map.entry("email", "Enter a valid email address."),
      Map.entry("countryCode", "Enter a valid mobile number."),
      Map.entry("mobileNumber", "Enter a valid mobile number."),
      Map.entry("mobile", "Enter a valid mobile number."),
      Map.entry("address", "Address must be at least 10 characters long."),
      Map.entry("username", "Username must be at least 5 characters and unique."),
      Map.entry("password", "Password must be at least 8 characters and include a mix of uppercase, lowercase, number, and special character."),
      Map.entry("category", "Please fill in all required fields."),
      Map.entry("bookingId", "Please fill in all required fields."),
      Map.entry("title", "Please provide more details to help us resolve your issue."),
      Map.entry("description", "Please provide more details to help us resolve your issue."),
      Map.entry("contactPreference", "Please fill in all required fields.")
  );

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiError> handleApi(ApiException ex) {
    if (ex.getField() != null) {
      return ResponseEntity.status(ex.getStatus()).body(new ApiError(ex.getField(), ex.getMessage()));
    }
    return ResponseEntity.status(ex.getStatus()).body(new ApiError(ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
    FieldError fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
    if (fieldError == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError("Invalid request."));
    }
    String field = fieldError.getField();
    String message = FIELD_MESSAGES.getOrDefault(field, "Invalid request.");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(field, message));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError("Invalid request."));
  }
}
