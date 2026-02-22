package com.hms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "customers",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "userId"),
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "mobile"),
        @UniqueConstraint(columnNames = "username")
    }
)
public class Customer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String userId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String mobile;

  @Column(nullable = false, length = 512)
  private String address;

  @Column(nullable = false)
  private String username;

  @Column(nullable = false)
  private String passwordHash;

  @Column(nullable = false)
  private int failedAttempts;

  @Column(nullable = false)
  private boolean locked;

  @Column(nullable = false)
  private boolean admin;

  @Column(nullable = false)
  private boolean passwordChangeRequired;

  public Long getId() {
    return id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public int getFailedAttempts() {
    return failedAttempts;
  }

  public void setFailedAttempts(int failedAttempts) {
    this.failedAttempts = failedAttempts;
  }

  public boolean isLocked() {
    return locked;
  }

  public void setLocked(boolean locked) {
    this.locked = locked;
  }

  public boolean isAdmin() {
    return admin;
  }

  public void setAdmin(boolean admin) {
    this.admin = admin;
  }

  public boolean isPasswordChangeRequired() {
    return passwordChangeRequired;
  }

  public void setPasswordChangeRequired(boolean passwordChangeRequired) {
    this.passwordChangeRequired = passwordChangeRequired;
  }
}
