package com.hms.api;

import com.hms.api.dto.LoginRequest;
import com.hms.api.dto.LoginResponse;
import com.hms.api.dto.RegisterRequest;
import com.hms.api.dto.RegisterResponse;
import com.hms.api.dto.ChangePasswordRequest;
import com.hms.api.dto.UpdateProfileRequest;
import com.hms.security.JwtService;
import com.hms.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
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
  private final JwtService jwtService;

  public CustomerController(CustomerService service, JwtService jwtService) {
    this.service = service;
    this.jwtService = jwtService;
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.register(request));
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    LoginResponse loginResponse = service.login(request);
    String token = jwtService.createToken(loginResponse.getUserId(), loginResponse.getRole());
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, jwtService.buildAuthCookie(token).toString())
        .body(loginResponse);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, jwtService.clearAuthCookie().toString())
        .build();
  }

  @GetMapping("/me")
  public ResponseEntity<LoginResponse> me(Authentication authentication) {
    if (authentication == null || authentication.getName() == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    return ResponseEntity.ok(service.getByUserId(authentication.getName()));
  }

  @PutMapping("/{userId}")
  public ResponseEntity<LoginResponse> updateProfile(
      @PathVariable String userId,
      @Valid @RequestBody UpdateProfileRequest request,
      Authentication authentication
  ) {
    if (authentication == null || authentication.getName() == null || !authentication.getName().equals(userId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok(service.updateProfile(userId, request));
  }

  @PostMapping("/change-password")
  public ResponseEntity<Void> changePassword(
      @Valid @RequestBody ChangePasswordRequest request,
      Authentication authentication
  ) {
    if (authentication == null || authentication.getName() == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    service.changePassword(authentication.getName(), request);
    return ResponseEntity.ok().build();
  }
}
