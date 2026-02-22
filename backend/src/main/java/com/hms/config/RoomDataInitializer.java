package com.hms.config;

import com.hms.domain.Room;
import com.hms.repo.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoomDataInitializer {
  @Bean
  CommandLineRunner initRooms(RoomRepository roomRepository) {
    return args -> {
      syncRoomImageUrls(roomRepository);
      if (roomRepository.count() > 0) {
        return;
      }
      roomRepository.save(createRoom(
          "STD-101",
          "Standard",
          "Queen",
          6000,
          2,
          1,
          "WiFi, TV, AC",
          280,
          "/assets/Standard.png"
      ));
      roomRepository.save(createRoom(
          "DLX-210",
          "Deluxe",
          "King",
          8000,
          3,
          2,
          "WiFi, TV, AC, Mini-bar",
          360,
          "/assets/Deluxe.png"
      ));
      roomRepository.save(createRoom(
          "STE-501",
          "Suite",
          "King",
          14000,
          4,
          2,
          "WiFi, TV, AC, Mini-bar, Balcony, Breakfast",
          540,
          "/assets/Suite.png"
      ));
    };
  }

  private void syncRoomImageUrls(RoomRepository roomRepository) {
    roomRepository.findByActiveTrue().forEach(room -> {
      String roomType = room.getRoomType();
      if ("Standard".equalsIgnoreCase(roomType)) {
        room.setImageUrl("/assets/Standard.png");
        if (room.getBedType() == null || room.getBedType().isBlank()) {
          room.setBedType("Queen");
        }
      } else if ("Deluxe".equalsIgnoreCase(roomType)) {
        room.setImageUrl("/assets/Deluxe.png");
        if (room.getBedType() == null || room.getBedType().isBlank()) {
          room.setBedType("King");
        }
      } else if ("Suite".equalsIgnoreCase(roomType)) {
        room.setImageUrl("/assets/Suite.png");
        if (room.getBedType() == null || room.getBedType().isBlank()) {
          room.setBedType("King");
        }
      }
      if (room.getRoomStatus() == null || room.getRoomStatus().isBlank()) {
        room.setRoomStatus("AVAILABLE");
      }
      roomRepository.save(room);
    });
  }

  private Room createRoom(
      String roomCode,
      String roomType,
      String bedType,
      int price,
      int occAdults,
      int occChildren,
      String amenities,
      int size,
      String imageUrl
  ) {
    Room room = new Room();
    room.setRoomCode(roomCode);
    room.setRoomType(roomType);
    room.setBedType(bedType);
    room.setPricePerNight(price);
    room.setOccupancyAdults(occAdults);
    room.setOccupancyChildren(occChildren);
    room.setAmenitiesCsv(amenities);
    room.setRoomSizeSqFt(size);
    room.setImageUrl(imageUrl);
    room.setActive(true);
    room.setRoomStatus("AVAILABLE");
    return room;
  }
}
