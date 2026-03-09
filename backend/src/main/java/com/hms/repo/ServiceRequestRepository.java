package com.hms.repo;

import com.hms.domain.ServiceRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
  Optional<ServiceRequest> findByRequestId(String requestId);
  boolean existsByRequestId(String requestId);
  boolean existsByTransactionId(String transactionId);
  List<ServiceRequest> findByCustomerUserIdOrderByCreatedAtDesc(String customerUserId);
  List<ServiceRequest> findByCustomerUserIdAndBookingIdOrderByCreatedAtDesc(String customerUserId, String bookingId);
  List<ServiceRequest> findAllByOrderByCreatedAtDesc();
}
