import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { RoomService } from '../../../core/services/room.service';
import { RoomSearchResult } from '../../../core/models/booking.model';

type RoomType = 'Standard' | 'Deluxe' | 'Suite';

interface RoomGroup {
  roomType: string;
  price: number;
  occupancyAdults: number;
  occupancyChildren: number;
  amenities: string[];
  roomSizeSqFt: number;
  imageUrl: string;
  available: boolean;
  firstAvailableRoomId?: string;
}

@Component({
  selector: 'app-search-availability',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf, NgFor],
  templateUrl: './search-availability.component.html'
})
export class SearchAvailabilityComponent {
  readonly today = this.formatDate(new Date());
  readonly roomTypes: RoomType[] = ['Standard', 'Deluxe', 'Suite'];
  readonly amenityOptions = [
    { key: 'wifi', label: 'WiFi' },
    { key: 'tv', label: 'TV' },
    { key: 'ac', label: 'AC' },
    { key: 'minibar', label: 'Mini-bar' },
    { key: 'balcony', label: 'Balcony' },
    { key: 'breakfast', label: 'Breakfast' }
  ];

  form = this.fb.group({
    checkIn: [''],
    checkOut: [''],
    adults: [1],
    children: [0],
    roomType: [''],
    priceMin: [0],
    priceMax: [20000],
    sizeMin: [0],
    sizeMax: [1000],
    sortBy: ['priceLow'],
    amenity_wifi: [false],
    amenity_tv: [false],
    amenity_ac: [false],
    amenity_minibar: [false],
    amenity_balcony: [false],
    amenity_breakfast: [false]
  });

  results: RoomSearchResult[] = [];
  searched = false;
  isSearching = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private roomService: RoomService
  ) {
    this.form = this.fb.group({
      checkIn: ['', [Validators.required, this.minTodayValidator.bind(this)]],
      checkOut: ['', [Validators.required]],
      adults: [1, [Validators.required, Validators.min(1), Validators.max(10)]],
      children: [0, [Validators.min(0), Validators.max(5)]],
      roomType: ['', [Validators.required]],
      priceMin: [0, [Validators.min(0)]],
      priceMax: [20000, [Validators.min(0)]],
      sizeMin: [0, [Validators.min(0)]],
      sizeMax: [1000, [Validators.min(0)]],
      sortBy: ['priceLow'],
      amenity_wifi: [false],
      amenity_tv: [false],
      amenity_ac: [false],
      amenity_minibar: [false],
      amenity_balcony: [false],
      amenity_breakfast: [false]
    }, { validators: [this.checkOutAfterCheckInValidator.bind(this)] });

    this.form.get('checkIn')?.valueChanges.subscribe((value) => {
      const checkIn = this.toDate(value);
      const checkOut = this.toDate(this.form.get('checkOut')?.value);
      if (checkIn && checkOut && checkOut < checkIn) {
        this.form.get('checkOut')?.setValue('', { emitEvent: false });
      }
    });
  }

  get minCheckout(): string {
    const checkIn = this.toDate(this.form.get('checkIn')?.value);
    if (!checkIn) {
      return this.addDays(this.toDate(this.today) ?? new Date(), 1);
    }
    return this.addDays(checkIn, 1);
  }

  search(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.searched = true;
    const criteria = this.form.getRawValue() as {
      roomType: string | null;
      adults: number | null;
      children: number | null;
      priceMin: number | null;
      priceMax: number | null;
      sizeMin: number | null;
      sizeMax: number | null;
      sortBy: string | null;
    };
    this.isSearching = true;
    this.roomService.search({
      checkInDate: String(this.form.value.checkIn ?? ''),
      checkOutDate: String(this.form.value.checkOut ?? ''),
      adults: Number(this.form.value.adults ?? 1),
      children: Number(this.form.value.children ?? 0),
      roomType: String(criteria.roomType ?? '')
    }).subscribe({
      next: (rooms) => {
        let filtered = rooms.filter((room) => {
          const meetsPrice = room.price >= (criteria.priceMin ?? 0) && room.price <= (criteria.priceMax ?? Number.MAX_SAFE_INTEGER);
          const meetsSize = room.roomSizeSqFt >= (criteria.sizeMin ?? 0) && room.roomSizeSqFt <= (criteria.sizeMax ?? Number.MAX_SAFE_INTEGER);
          return meetsPrice && meetsSize;
        });

        const requiredAmenities = this.amenityOptions
          .filter((amenity) => Boolean(this.form.get(`amenity_${amenity.key}`)?.value))
          .map((amenity) => amenity.label);
        if (requiredAmenities.length > 0) {
          filtered = filtered.filter((room) => requiredAmenities.every((amenity) => room.amenities.includes(amenity)));
        }

        filtered = this.sortRooms(filtered, String(criteria.sortBy));
        this.results = filtered;
        this.isSearching = false;
      },
      error: () => {
        this.results = [];
        this.isSearching = false;
      }
    });
  }

  book(room: RoomGroup): void {
    if (!room.available || !room.firstAvailableRoomId) {
      return;
    }
    const criteria = this.form.getRawValue() as {
      checkIn: string | null;
      checkOut: string | null;
      adults: number | null;
      children: number | null;
    };
    this.router.navigate(['/booking/confirm'], {
      queryParams: {
        roomId: room.firstAvailableRoomId,
        roomType: room.roomType,
        price: room.price,
        occAdults: room.occupancyAdults,
        occChildren: room.occupancyChildren,
        checkIn: criteria.checkIn ?? '',
        checkOut: criteria.checkOut ?? '',
        adults: criteria.adults ?? 1,
        children: criteria.children ?? 0
      }
    });
  }

  private sortRooms(rooms: RoomSearchResult[], sortBy: string): RoomSearchResult[] {
    const copy = [...rooms];
    if (sortBy === 'priceHigh') {
      return copy.sort((a, b) => b.price - a.price);
    }
    if (sortBy === 'availability') {
      return copy.sort((a, b) => Number(b.available) - Number(a.available));
    }
    return copy.sort((a, b) => a.price - b.price);
  }

  private minTodayValidator(control: AbstractControl): ValidationErrors | null {
    const value = this.toDate(control.value);
    if (!value) {
      return null;
    }
    const today = this.toDate(this.today);
    if (!today) {
      return null;
    }
    return value < today ? { pastDate: true } : null;
  }

  private checkOutAfterCheckInValidator(control: AbstractControl): ValidationErrors | null {
    const checkIn = this.toDate(control.get('checkIn')?.value);
    const checkOut = this.toDate(control.get('checkOut')?.value);
    if (!checkIn || !checkOut) {
      return null;
    }
    return checkOut <= checkIn ? { checkOutBeforeCheckIn: true } : null;
  }

  private toDate(value: string | null | undefined): Date | null {
    if (!value) {
      return null;
    }
    const date = new Date(String(value));
    return Number.isNaN(date.getTime()) ? null : date;
  }

  private addDays(date: Date, days: number): string {
    const next = new Date(date);
    next.setDate(next.getDate() + days);
    return this.formatDate(next);
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  get groupedResults(): RoomGroup[] {
    const map = new Map<string, RoomGroup>();
    this.results.forEach((room) => {
      const existing = map.get(room.roomType);
      if (!existing) {
        map.set(room.roomType, {
          roomType: room.roomType,
          price: room.price,
          occupancyAdults: room.occupancyAdults,
          occupancyChildren: room.occupancyChildren,
          amenities: room.amenities,
          roomSizeSqFt: room.roomSizeSqFt,
          imageUrl: room.imageUrl,
          available: room.available,
          firstAvailableRoomId: room.available ? room.roomId : undefined
        });
        return;
      }
      if (room.available && !existing.available) {
        existing.available = true;
        existing.firstAvailableRoomId = room.roomId;
      }
    });
    return Array.from(map.values());
  }
}
