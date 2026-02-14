package com.hms.api;

import com.hms.api.dto.BookingResponse;
import com.hms.api.dto.CancellationPreviewResponse;
import com.hms.api.dto.InvoiceResponse;
import com.hms.api.dto.ModifyBookingConfirmRequest;
import com.hms.api.dto.ModifyBookingPreviewResponse;
import com.hms.api.dto.ModifyBookingRequest;
import com.hms.api.dto.PaymentRequest;
import com.hms.service.BookingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
  private final BookingService service;

  public BookingController(BookingService service) {
    this.service = service;
  }

  @PostMapping("/pay")
  public ResponseEntity<BookingResponse> pay(@Valid @RequestBody PaymentRequest request) {
    return ResponseEntity.ok(service.payAndCreateBooking(request));
  }

  @GetMapping
  public ResponseEntity<List<BookingResponse>> list(@RequestParam String userId) {
    return ResponseEntity.ok(service.listByUser(userId));
  }

  @PatchMapping("/{bookingId}/cancel")
  public ResponseEntity<BookingResponse> cancel(@PathVariable String bookingId, @RequestParam String userId) {
    return ResponseEntity.ok(service.cancel(bookingId, userId));
  }

  @GetMapping("/{bookingId}/invoice")
  public ResponseEntity<InvoiceResponse> invoice(@PathVariable String bookingId, @RequestParam String userId) {
    return ResponseEntity.ok(service.getInvoice(bookingId, userId));
  }

  @GetMapping("/{bookingId}/cancel-preview")
  public ResponseEntity<CancellationPreviewResponse> cancellationPreview(
      @PathVariable String bookingId,
      @RequestParam String userId
  ) {
    return ResponseEntity.ok(service.cancellationPreview(bookingId, userId));
  }

  @PostMapping("/{bookingId}/modify/preview")
  public ResponseEntity<ModifyBookingPreviewResponse> previewModification(
      @PathVariable String bookingId,
      @Valid @RequestBody ModifyBookingRequest request
  ) {
    return ResponseEntity.ok(service.previewModification(bookingId, request));
  }

  @PostMapping("/{bookingId}/modify/confirm")
  public ResponseEntity<BookingResponse> confirmModification(
      @PathVariable String bookingId,
      @Valid @RequestBody ModifyBookingConfirmRequest request
  ) {
    return ResponseEntity.ok(service.confirmModification(bookingId, request));
  }
}
