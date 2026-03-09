package com.hms.api;

import com.hms.api.dto.CabServiceCreateRequest;
import com.hms.api.dto.DiningServiceCreateRequest;
import com.hms.api.dto.SalonServiceCreateRequest;
import com.hms.api.dto.ServiceCatalogResponse;
import com.hms.api.dto.ServiceRequestResponse;
import com.hms.service.ServiceRequestService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/services")
public class ServiceController {
  private final ServiceRequestService service;

  public ServiceController(ServiceRequestService service) {
    this.service = service;
  }

  @GetMapping("/catalog")
  public ResponseEntity<ServiceCatalogResponse> catalog() {
    return ResponseEntity.ok(service.catalog());
  }

  @PostMapping("/cab")
  public ResponseEntity<ServiceRequestResponse> createCab(
      @Valid @RequestBody CabServiceCreateRequest request,
      Authentication authentication
  ) {
    return ResponseEntity.ok(service.createCab(authentication.getName(), request));
  }

  @PostMapping("/salon")
  public ResponseEntity<ServiceRequestResponse> createSalon(
      @Valid @RequestBody SalonServiceCreateRequest request,
      Authentication authentication
  ) {
    return ResponseEntity.ok(service.createSalon(authentication.getName(), request));
  }

  @PostMapping("/dining")
  public ResponseEntity<ServiceRequestResponse> createDining(
      @Valid @RequestBody DiningServiceCreateRequest request,
      Authentication authentication
  ) {
    return ResponseEntity.ok(service.createDining(authentication.getName(), request));
  }

  @GetMapping("/my")
  public ResponseEntity<List<ServiceRequestResponse>> myServices(
      @RequestParam(name = "bookingId", required = false) String bookingId,
      Authentication authentication
  ) {
    return ResponseEntity.ok(service.listMy(authentication.getName(), bookingId));
  }
}
