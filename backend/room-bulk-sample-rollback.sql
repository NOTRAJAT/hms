-- Rollback for backend/room-bulk-sample.csv
-- Deletes only demo-imported rooms and skips rooms already linked to bookings.
-- Note: bookings table links by roomCode (not room_id).

DELETE r
FROM rooms r
LEFT JOIN bookings b ON b.roomCode = r.roomCode
WHERE r.description LIKE 'BULK_DEMO_20260221%'
  AND b.id IS NULL;
