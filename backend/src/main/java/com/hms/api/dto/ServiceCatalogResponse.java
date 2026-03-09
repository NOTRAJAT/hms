package com.hms.api.dto;

import java.util.List;

public class ServiceCatalogResponse {
  private final List<CabDestinationOption> cabDestinations;
  private final List<SalonPackageOption> salonPackages;
  private final List<DiningMenuOption> diningMenu;

  public ServiceCatalogResponse(
      List<CabDestinationOption> cabDestinations,
      List<SalonPackageOption> salonPackages,
      List<DiningMenuOption> diningMenu
  ) {
    this.cabDestinations = cabDestinations;
    this.salonPackages = salonPackages;
    this.diningMenu = diningMenu;
  }

  public List<CabDestinationOption> getCabDestinations() {
    return cabDestinations;
  }

  public List<SalonPackageOption> getSalonPackages() {
    return salonPackages;
  }

  public List<DiningMenuOption> getDiningMenu() {
    return diningMenu;
  }

  public static class CabDestinationOption {
    private final String destination;
    private final int fare;

    public CabDestinationOption(String destination, int fare) {
      this.destination = destination;
      this.fare = fare;
    }

    public String getDestination() {
      return destination;
    }

    public int getFare() {
      return fare;
    }
  }

  public static class SalonPackageOption {
    private final String packageCode;
    private final String packageName;
    private final int price;
    private final int durationMinutes;

    public SalonPackageOption(String packageCode, String packageName, int price, int durationMinutes) {
      this.packageCode = packageCode;
      this.packageName = packageName;
      this.price = price;
      this.durationMinutes = durationMinutes;
    }

    public String getPackageCode() {
      return packageCode;
    }

    public String getPackageName() {
      return packageName;
    }

    public int getPrice() {
      return price;
    }

    public int getDurationMinutes() {
      return durationMinutes;
    }
  }

  public static class DiningMenuOption {
    private final String itemCode;
    private final String itemName;
    private final int price;

    public DiningMenuOption(String itemCode, String itemName, int price) {
      this.itemCode = itemCode;
      this.itemName = itemName;
      this.price = price;
    }

    public String getItemCode() {
      return itemCode;
    }

    public String getItemName() {
      return itemName;
    }

    public int getPrice() {
      return price;
    }
  }
}
