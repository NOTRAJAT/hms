package com.hms.service;

import com.hms.api.ApiException;
import com.hms.api.dto.LoginRequest;
import com.hms.api.dto.LoginResponse;
import com.hms.api.dto.RegisterRequest;
import com.hms.api.dto.RegisterResponse;
import com.hms.api.dto.ChangePasswordRequest;
import com.hms.api.dto.UpdateProfileRequest;
import com.hms.domain.Customer;
import com.hms.domain.Staff;
import com.hms.repo.CustomerRepository;
import com.hms.repo.StaffRepository;
import java.security.SecureRandom;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {
  private static final int MAX_FAILED_ATTEMPTS = 3;
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z ]{2,50}$");
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
  private static final Pattern MOBILE_WITH_COUNTRY_CODE_PATTERN = Pattern.compile("^\\+[0-9]{1,3}[0-9]{10}$");

  private final CustomerRepository repository;
  private final StaffRepository staffRepository;
  private final PasswordEncoder encoder;

  public CustomerService(CustomerRepository repository, StaffRepository staffRepository, PasswordEncoder encoder) {
    this.repository = repository;
    this.staffRepository = staffRepository;
    this.encoder = encoder;
  }

  @Transactional
  public RegisterResponse register(RegisterRequest request) {
    String name = request.getName().trim();
    String email = request.getEmail().trim().toLowerCase();
    String username = request.getUsername().trim().toLowerCase();
    String address = request.getAddress().trim();
    String countryCode = request.getCountryCode().trim();
    String mobileNumber = request.getMobileNumber().trim();
    String mobile = countryCode + mobileNumber;

    if (!isStrongPassword(request.getPassword())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "password",
          "Password must be at least 8 characters and include a mix of uppercase, lowercase, number, and special character.");
    }

    if (repository.existsByEmail(email) || staffRepository.existsByEmail(email)) {
      throw new ApiException(HttpStatus.CONFLICT, "email", "Email already registered");
    }
    if (repository.existsByMobile(mobile) || staffRepository.existsByMobile(mobile)) {
      throw new ApiException(HttpStatus.CONFLICT, "mobileNumber", "Mobile number already registered.");
    }
    if (repository.existsByUsername(username) || staffRepository.existsByUsername(username)) {
      throw new ApiException(HttpStatus.CONFLICT, "username", "Username must be at least 5 characters and unique.");
    }

    Customer customer = new Customer();
    customer.setUserId(generateUserId());
    customer.setName(name);
    customer.setEmail(email);
    customer.setMobile(mobile);
    customer.setAddress(address);
    customer.setUsername(username);
    customer.setPasswordHash(encoder.encode(request.getPassword()));
    customer.setFailedAttempts(0);
    customer.setLocked(false);
    customer.setAdmin(false);
    customer.setPasswordChangeRequired(false);

    repository.save(customer);
    return new RegisterResponse(customer.getUserId(), customer.getName(), customer.getEmail());
  }

  @Transactional(noRollbackFor = ApiException.class)
  public LoginResponse login(LoginRequest request) {
    String username = request.getUsername().trim().toLowerCase();
    Customer customer = repository.findByUsername(username).orElse(null);
    if (customer != null) {
      return loginCustomer(request, customer);
    }
    Staff staff = staffRepository.findByUsername(username).orElse(null);
    if (staff != null) {
      return loginStaff(request, staff);
    }
    throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
  }

  @Transactional
  public LoginResponse updateProfile(String userId, UpdateProfileRequest request) {
    String name = sanitize(request.getName());
    String email = sanitize(request.getEmail()).toLowerCase();
    String mobile = sanitize(request.getMobile());
    String address = sanitize(request.getAddress());

    if (name.isBlank() || email.isBlank() || mobile.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Please fill in all required fields.");
    }
    if (!NAME_PATTERN.matcher(name).matches()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "name",
          "Full Name must be 2-50 letters and spaces only.");
    }
    if (!EMAIL_PATTERN.matcher(email).matches()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "email",
          "Please enter a valid email address.");
    }
    if (!MOBILE_WITH_COUNTRY_CODE_PATTERN.matcher(mobile).matches()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "mobile",
          "Please enter a valid phone number with country code.");
    }
    if (address.length() > 100) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "address",
          "Address must be 100 characters or fewer.");
    }

    Customer customer = repository.findByUserId(userId).orElse(null);
    if (customer != null) {
      if (repository.existsByEmailAndUserIdNot(email, userId) || staffRepository.existsByEmail(email)) {
        throw new ApiException(HttpStatus.CONFLICT, "email", "Email already registered");
      }
      if (repository.existsByMobileAndUserIdNot(mobile, userId) || staffRepository.existsByMobile(mobile)) {
        throw new ApiException(HttpStatus.CONFLICT, "mobile", "Mobile number already registered.");
      }
      customer.setName(name);
      customer.setEmail(email);
      customer.setMobile(mobile);
      customer.setAddress(address);
      repository.save(customer);
      return toLoginResponse(customer);
    }

    Staff staff = staffRepository.findByUserId(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    if (staffRepository.existsByEmailAndUserIdNot(email, userId) || repository.existsByEmail(email)) {
      throw new ApiException(HttpStatus.CONFLICT, "email", "Email already registered");
    }
    if (staffRepository.existsByMobileAndUserIdNot(mobile, userId) || repository.existsByMobile(mobile)) {
      throw new ApiException(HttpStatus.CONFLICT, "mobile", "Mobile number already registered.");
    }
    staff.setName(name);
    staff.setEmail(email);
    staff.setMobile(mobile);
    staff.setAddress(address);
    staffRepository.save(staff);
    return toLoginResponse(staff);
  }

  @Transactional(readOnly = true)
  public LoginResponse getByUserId(String userId) {
    Customer customer = repository.findByUserId(userId).orElse(null);
    if (customer != null) {
      return toLoginResponse(customer);
    }
    Staff staff = staffRepository.findByUserId(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    return toLoginResponse(staff);
  }

  @Transactional
  public void changePassword(String userId, ChangePasswordRequest request) {
    if (!isStrongPassword(request.getNewPassword())) {
      throw new ApiException(HttpStatus.BAD_REQUEST,
          "Password must be at least 8 characters and include a mix of uppercase, lowercase, number, and special character.");
    }
    Customer customer = repository.findByUserId(userId).orElse(null);
    if (customer != null) {
      if (!encoder.matches(request.getCurrentPassword(), customer.getPasswordHash())) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Current password is incorrect.");
      }
      customer.setPasswordHash(encoder.encode(request.getNewPassword()));
      customer.setPasswordChangeRequired(false);
      repository.save(customer);
      return;
    }
    Staff staff = staffRepository.findByUserId(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    if (!encoder.matches(request.getCurrentPassword(), staff.getPasswordHash())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Current password is incorrect.");
    }
    staff.setPasswordHash(encoder.encode(request.getNewPassword()));
    staff.setPasswordChangeRequired(false);
    staffRepository.save(staff);
  }

  private String generateUserId() {
    String candidate;
    do {
      candidate = String.format("CUST-%06d", RANDOM.nextInt(1_000_000));
    } while (repository.existsByUserId(candidate));
    return candidate;
  }

  private boolean isStrongPassword(String value) {
    if (value == null || value.length() < 8) {
      return false;
    }
    boolean hasUpper = value.chars().anyMatch(Character::isUpperCase);
    boolean hasLower = value.chars().anyMatch(Character::isLowerCase);
    boolean hasDigit = value.chars().anyMatch(Character::isDigit);
    boolean hasSpecial = value.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
    return hasUpper && hasLower && hasDigit && hasSpecial;
  }

  private String sanitize(String value) {
    return value == null ? "" : value.trim();
  }

  private LoginResponse loginCustomer(LoginRequest request, Customer customer) {
    if (customer.isLocked()) {
      throw new ApiException(HttpStatus.LOCKED, "Your account is locked. Please contact support.");
    }
    if (!encoder.matches(request.getPassword(), customer.getPasswordHash())) {
      int attempts = customer.getFailedAttempts() + 1;
      customer.setFailedAttempts(attempts);
      if (attempts >= MAX_FAILED_ATTEMPTS) {
        customer.setLocked(true);
      }
      repository.save(customer);
      if (customer.isLocked()) {
        throw new ApiException(HttpStatus.LOCKED, "Your account is locked. Please contact support.");
      }
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
    }
    customer.setFailedAttempts(0);
    customer.setLocked(false);
    repository.save(customer);
    return toLoginResponse(customer);
  }

  private LoginResponse loginStaff(LoginRequest request, Staff staff) {
    if (staff.isLocked()) {
      throw new ApiException(HttpStatus.LOCKED, "Your account is locked. Please contact support.");
    }
    if (!encoder.matches(request.getPassword(), staff.getPasswordHash())) {
      int attempts = staff.getFailedAttempts() + 1;
      staff.setFailedAttempts(attempts);
      if (attempts >= MAX_FAILED_ATTEMPTS) {
        staff.setLocked(true);
      }
      staffRepository.save(staff);
      if (staff.isLocked()) {
        throw new ApiException(HttpStatus.LOCKED, "Your account is locked. Please contact support.");
      }
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
    }
    staff.setFailedAttempts(0);
    staff.setLocked(false);
    staffRepository.save(staff);
    return toLoginResponse(staff);
  }

  private LoginResponse toLoginResponse(Customer customer) {
    return new LoginResponse(
        customer.getUserId(),
        customer.getName(),
        customer.getEmail(),
        customer.getMobile(),
        customer.getAddress(),
        customer.isAdmin() ? "ADMIN" : "CUSTOMER",
        customer.isPasswordChangeRequired()
    );
  }

  private LoginResponse toLoginResponse(Staff staff) {
    return new LoginResponse(
        staff.getUserId(),
        staff.getName(),
        staff.getEmail(),
        staff.getMobile(),
        staff.getAddress(),
        "STAFF",
        staff.isPasswordChangeRequired()
    );
  }
}
