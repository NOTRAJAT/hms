package com.hms.repo;

import com.hms.domain.Complaint;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
  List<Complaint> findByUserIdOrderByCreatedAtDesc(String userId);
  Optional<Complaint> findByComplaintIdAndUserId(String complaintId, String userId);
}
