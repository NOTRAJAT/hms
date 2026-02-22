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
    name = "rooms",
    uniqueConstraints = { @UniqueConstraint(columnNames = "roomCode") }
)
public class Room {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String roomCode;

  @Column(nullable = false)
  private String roomType;

  @Column
  private String bedType;

  @Column(nullable = false)
  private int pricePerNight;

  @Column(nullable = false)
  private int occupancyAdults;

  @Column(nullable = false)
  private int occupancyChildren;

  @Column(nullable = false, length = 1000)
  private String amenitiesCsv;

  @Column(nullable = false)
  private int roomSizeSqFt;

  @Column(length = 500)
  private String description;

  @Column(nullable = false)
  private String imageUrl;

  @Column(nullable = false)
  private boolean active;

  @Column
  private String roomStatus;

  public Long getId() {
    return id;
  }

  public String getRoomCode() {
    return roomCode;
  }

  public void setRoomCode(String roomCode) {
    this.roomCode = roomCode;
  }

  public String getRoomType() {
    return roomType;
  }

  public void setRoomType(String roomType) {
    this.roomType = roomType;
  }

  public String getBedType() {
    return bedType;
  }

  public void setBedType(String bedType) {
    this.bedType = bedType;
  }

  public int getPricePerNight() {
    return pricePerNight;
  }

  public void setPricePerNight(int pricePerNight) {
    this.pricePerNight = pricePerNight;
  }

  public int getOccupancyAdults() {
    return occupancyAdults;
  }

  public void setOccupancyAdults(int occupancyAdults) {
    this.occupancyAdults = occupancyAdults;
  }

  public int getOccupancyChildren() {
    return occupancyChildren;
  }

  public void setOccupancyChildren(int occupancyChildren) {
    this.occupancyChildren = occupancyChildren;
  }

  public String getAmenitiesCsv() {
    return amenitiesCsv;
  }

  public void setAmenitiesCsv(String amenitiesCsv) {
    this.amenitiesCsv = amenitiesCsv;
  }

  public int getRoomSizeSqFt() {
    return roomSizeSqFt;
  }

  public void setRoomSizeSqFt(int roomSizeSqFt) {
    this.roomSizeSqFt = roomSizeSqFt;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public String getRoomStatus() {
    return roomStatus;
  }

  public void setRoomStatus(String roomStatus) {
    this.roomStatus = roomStatus;
  }
}
