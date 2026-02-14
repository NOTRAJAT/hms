package com.hms.repo;

import com.hms.domain.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
  Optional<Customer> findByUsername(String username);
  Optional<Customer> findByUserId(String userId);
  boolean existsByUserId(String userId);
  boolean existsByEmail(String email);
  boolean existsByMobile(String mobile);
  boolean existsByUsername(String username);
  boolean existsByEmailAndUserIdNot(String email, String userId);
  boolean existsByMobileAndUserIdNot(String mobile, String userId);
}
