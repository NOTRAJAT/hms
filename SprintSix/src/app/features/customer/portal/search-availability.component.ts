import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { RoomService } from '../../../core/services/room.service';
import { BookingResponse, RoomSearchResult } from '../../../core/models/booking.model';
import { BookingApiService } from '../../../core/services/booking-api.service';
import { AuthSessionService } from '../../../core/services/auth-session.service';

interface RoomGroup {
  roomType: string;
  price: number;
  minPrice: number;
  maxPrice: number;
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
  imports: [ReactiveFormsModule, NgIf, NgFor, DatePipe],
  templateUrl: './search-availability.component.html'
})
export class SearchAvailabilityComponent implements OnInit {
  readonly today = this.formatDate(new Date());
  readonly fallbackRoomTypes: string[] = ['Standard', 'Deluxe', 'Suite', 'Supreme'];
  roomTypes: string[] = [...this.fallbackRoomTypes];
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
    roomType: ['ALL'],
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
  userBookings: BookingResponse[] = [];
  overlappingUserBookings: BookingResponse[] = [];
  searched = false;
  isSearching = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private roomService: RoomService,
    private bookingApi: BookingApiService,
    private session: AuthSessionService
  ) {
    this.form = this.fb.group({
      checkIn: ['', [Validators.required, this.minTodayValidator.bind(this)]],
      checkOut: ['', [Validators.required]],
      adults: [1, [Validators.required, Validators.min(1), Validators.max(10)]],
      children: [0, [Validators.min(0), Validators.max(5)]],
      roomType: ['ALL', [Validators.required]],
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

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      const requestedType = String(params.get('roomType') ?? '').trim();
      if (requestedType) {
        this.applyRoomTypeFromQuery(requestedType);
      }
    });

    this.roomService.roomTypes().subscribe({
      next: (types) => {
        const normalized = (types || [])
          .map((type) => String(type ?? '').trim())
          .filter((type) => !!type);
        this.roomTypes = Array.from(new Set([...this.fallbackRoomTypes, ...normalized]));
        const requestedType = String(this.route.snapshot.queryParamMap.get('roomType') ?? '').trim();
        if (requestedType) {
          this.applyRoomTypeFromQuery(requestedType);
        }
      },
      error: () => {
        this.roomTypes = [...this.fallbackRoomTypes];
      }
    });

    this.loadUserBookings();
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
    this.overlappingUserBookings = [];
    const selectedCheckIn = String(this.form.value.checkIn ?? '');
    const selectedCheckOut = String(this.form.value.checkOut ?? '');
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
      checkInDate: selectedCheckIn,
      checkOutDate: selectedCheckOut,
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
        this.refreshOverlappingUserBookings(selectedCheckIn, selectedCheckOut);
        this.isSearching = false;
      },
      error: () => {
        this.results = [];
        this.overlappingUserBookings = [];
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

  private applyRoomTypeFromQuery(roomType: string): void {
    const requested = roomType.toLowerCase();
    const match = this.roomTypes.find((type) => type.toLowerCase() === requested);
    if (match) {
      this.form.get('roomType')?.setValue(match, { emitEvent: false });
    }
  }

  get groupedResults(): RoomGroup[] {
    const map = new Map<string, RoomGroup>();
    this.results.forEach((room) => {
      const existing = map.get(room.roomType);
      if (!existing) {
        map.set(room.roomType, {
          roomType: room.roomType,
          price: room.price,
          minPrice: room.price,
          maxPrice: room.price,
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
      existing.minPrice = Math.min(existing.minPrice, room.price);
      existing.maxPrice = Math.max(existing.maxPrice, room.price);
      if (room.available && !existing.available) {
        existing.available = true;
        existing.firstAvailableRoomId = room.roomId;
        existing.price = room.price;
      }
    });
    return Array.from(map.values());
  }

  private loadUserBookings(): void {
    const userId = this.session.value?.userId;
    if (!userId) {
      this.userBookings = [];
      return;
    }
    this.bookingApi.list(userId).subscribe({
      next: (items) => {
        this.userBookings = Array.isArray(items) ? items : [];
      },
      error: () => {
        this.userBookings = [];
      }
    });
  }

  private refreshOverlappingUserBookings(checkIn: string, checkOut: string): void {
    const start = this.toDate(checkIn);
    const end = this.toDate(checkOut);
    if (!start || !end) {
      this.overlappingUserBookings = [];
      return;
    }
    this.overlappingUserBookings = this.userBookings.filter((booking) => {
      if (String(booking.status).toLowerCase() === 'cancelled') {
        return false;
      }
      const bookingStart = this.toDate(String(booking.checkInDate).slice(0, 10));
      const bookingEnd = this.toDate(String(booking.checkOutDate).slice(0, 10));
      if (!bookingStart || !bookingEnd) {
        return false;
      }
      return bookingStart < end && start < bookingEnd;
    });
  }
}
