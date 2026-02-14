package com.hms.api;

import com.hms.api.dto.ComplaintRequest;
import com.hms.api.dto.ComplaintResponse;
import com.hms.service.ComplaintService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {
  private final ComplaintService service;

  public ComplaintController(ComplaintService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<List<ComplaintResponse>> list(@RequestParam String userId) {
    return ResponseEntity.ok(service.listByUser(userId));
  }

  @GetMapping("/{complaintId}")
  public ResponseEntity<ComplaintResponse> detail(@PathVariable String complaintId, @RequestParam String userId) {
    return ResponseEntity.ok(service.detail(complaintId, userId));
  }

  @PostMapping
  public ResponseEntity<ComplaintResponse> create(@Valid @RequestBody ComplaintRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
  }

  @PutMapping("/{complaintId}")
  public ResponseEntity<ComplaintResponse> update(
      @PathVariable String complaintId,
      @Valid @RequestBody ComplaintRequest request
  ) {
    return ResponseEntity.ok(service.update(complaintId, request));
  }

  @PatchMapping("/{complaintId}/confirm")
  public ResponseEntity<ComplaintResponse> confirm(@PathVariable String complaintId, @RequestParam String userId) {
    return ResponseEntity.ok(service.confirmResolution(complaintId, userId));
  }

  @PatchMapping("/{complaintId}/reopen")
  public ResponseEntity<ComplaintResponse> reopen(@PathVariable String complaintId, @RequestParam String userId) {
    return ResponseEntity.ok(service.reopen(complaintId, userId));
  }
}
