package com.hms.repo;

import com.hms.domain.Room;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
  List<Room> findByActiveTrueAndRoomType(String roomType);
  List<Room> findByActiveTrue();
  Optional<Room> findByRoomCode(String roomCode);
}
