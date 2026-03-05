package com.hms.config;

import com.hms.domain.Customer;
import com.hms.repo.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminDataInitializer {
  @Bean
  CommandLineRunner initAdminUser(CustomerRepository repository, PasswordEncoder encoder) {
    return args -> {
      if (repository.findByUsername("admin").isPresent()) {
        return;
      }
      Customer admin = new Customer();
      admin.setUserId("ADMIN-000001");
      admin.setName("System Admin");
      admin.setEmail("admin@hms.local");
      admin.setMobile("+919111111111");
      admin.setAddress("Admin HQ");
      admin.setUsername("admin");
      admin.setPasswordHash(encoder.encode("Admin@123"));
      admin.setFailedAttempts(0);
      admin.setLocked(false);
      admin.setAdmin(true);
      repository.save(admin);
    };
  }
}
