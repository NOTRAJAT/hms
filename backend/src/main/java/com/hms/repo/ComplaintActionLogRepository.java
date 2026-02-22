package com.hms.repo;

import com.hms.domain.ComplaintActionLog;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintActionLogRepository extends JpaRepository<ComplaintActionLog, Long> {
  List<ComplaintActionLog> findByComplaintIdOrderByActionAtDesc(String complaintId);
  Optional<ComplaintActionLog> findFirstByComplaintIdOrderByActionAtDesc(String complaintId);
}
