package com.hms.repo;

import com.hms.domain.Staff;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Long> {
  Optional<Staff> findByUsername(String username);
  Optional<Staff> findByUserId(String userId);
  boolean existsByUserId(String userId);
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);
  boolean existsByMobile(String mobile);
  boolean existsByEmailAndUserIdNot(String email, String userId);
  boolean existsByMobileAndUserIdNot(String mobile, String userId);
}

