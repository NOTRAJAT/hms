package com.hms.api;

import com.hms.api.dto.RoomSearchResponse;
import com.hms.service.RoomService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
  private final RoomService service;

  public RoomController(RoomService service) {
    this.service = service;
  }

  @GetMapping("/search")
  public ResponseEntity<List<RoomSearchResponse>> search(
      @RequestParam LocalDate checkInDate,
      @RequestParam LocalDate checkOutDate,
      @RequestParam int adults,
      @RequestParam int children,
      @RequestParam String roomType
  ) {
    return ResponseEntity.ok(service.search(checkInDate, checkOutDate, adults, children, roomType));
  }
}
