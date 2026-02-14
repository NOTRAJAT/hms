package com.hms.api;

import com.hms.api.dto.LoginRequest;
import com.hms.api.dto.LoginResponse;
import com.hms.api.dto.RegisterRequest;
import com.hms.api.dto.RegisterResponse;
import com.hms.api.dto.UpdateProfileRequest;
import com.hms.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
  private final CustomerService service;

  public CustomerController(CustomerService service) {
    this.service = service;
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.register(request));
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(service.login(request));
  }

  @PutMapping("/{userId}")
  public ResponseEntity<LoginResponse> updateProfile(
      @PathVariable String userId,
      @Valid @RequestBody UpdateProfileRequest request
  ) {
    return ResponseEntity.ok(service.updateProfile(userId, request));
  }
}
