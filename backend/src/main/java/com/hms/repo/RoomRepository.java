package com.hms.repo;

import com.hms.domain.Room;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoomRepository extends JpaRepository<Room, Long> {
  List<Room> findByActiveTrueAndRoomType(String roomType);
  List<Room> findByActiveTrue();
  @Query("select distinct r.roomType from Room r where r.active = true order by r.roomType asc")
  List<String> findDistinctActiveRoomTypes();
  List<Room> findAllByOrderByRoomCodeAsc();
  List<Room> findByRoomCodeContainingIgnoreCaseOrRoomTypeContainingIgnoreCaseOrderByRoomCodeAsc(String roomCode, String roomType);
  Optional<Room> findByRoomCode(String roomCode);
  long countByActiveTrue();
}
