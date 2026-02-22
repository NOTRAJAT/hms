package com.hms.repo;

import com.hms.domain.BillingRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingRecordRepository extends JpaRepository<BillingRecord, Long> {
  Optional<BillingRecord> findByBillId(String billId);
  boolean existsByBillId(String billId);
  List<BillingRecord> findAllByOrderByIssueDateDesc();
}

