import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { AdminRoomCreatePayload, AdminRoomItem, AdminRoomUpdatePayload } from '../../core/models/admin.model';
import { AdminService } from '../../core/services/admin.service';

@Component({
  selector: 'app-admin-rooms',
  standalone: true,
  imports: [FormsModule, NgFor, NgIf, DatePipe],
  templateUrl: './admin-rooms.component.html'
})
export class AdminRoomsComponent implements OnInit {
  readonly allowedAmenities = ['WiFi', 'TV', 'Mini-bar', 'AC', 'Balcony', 'Breakfast'];
  rooms: AdminRoomItem[] = [];
  totalItems = 0;
  totalPages = 0;
  page = 0;
  readonly size = 10;

  q = '';
  roomType = '';
  availability: '' | 'AVAILABLE' | 'NOT_AVAILABLE' = '';
  amenity = '';
  date = '';
  priceMin?: number;
  priceMax?: number;
  maxOccupancy?: number;
  sortBy = 'roomCode';
  sortDir: 'asc' | 'desc' = 'asc';

  selected: AdminRoomItem | null = null;
  editForm: AdminRoomUpdatePayload = {
    bedType: 'Queen',
    pricePerNight: 1,
    roomStatus: 'AVAILABLE',
    amenitiesCsv: '',
    occupancyAdults: 1,
    occupancyChildren: 0
  };

  successMessage = '';
  errorMessage = '';
  loading = false;
  showConfirmModal = false;
  updating = false;
  editErrorMessage = '';
  selectedAmenities: string[] = [];
  addMode = false;
  addSubmitted = false;
  addSuccessMessage = '';
  addGeneratedRoomCode = '';
  bulkFile: File | null = null;
  addTouched = {
    pricePerNight: false,
    occupancyAdults: false,
    occupancyChildren: false,
    description: false
  };

  addForm: AdminRoomCreatePayload = {
    roomType: 'Standard',
    bedType: 'Queen',
    pricePerNight: 1000,
    amenitiesCsv: '',
    availability: 'Available',
    occupancyAdults: 1,
    occupancyChildren: 0,
    description: ''
  };
  addAmenities: string[] = [];

  constructor(private adminService: AdminService, private router: Router) {}

  ngOnInit(): void {
    this.load();
  }

  load(resetPage = false): void {
    if (resetPage) {
      this.page = 0;
    }
    this.loading = true;
    this.successMessage = '';
    this.errorMessage = '';
    this.adminService.rooms({
      q: this.q.trim() || undefined,
      roomType: this.roomType || undefined,
      availability: this.date ? (this.availability || undefined) : undefined,
      amenity: this.amenity.trim() || undefined,
      date: this.date || undefined,
      priceMin: this.priceMin,
      priceMax: this.priceMax,
      maxOccupancy: this.maxOccupancy,
      sortBy: this.sortBy,
      sortDir: this.sortDir,
      page: this.page,
      size: this.size
    }).subscribe({
      next: (response) => {
        this.rooms = response.items;
        this.totalItems = response.totalItems;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = error?.error?.error ?? 'Unable to fetch rooms.';
        this.loading = false;
      }
    });
  }

  toggleSort(column: string): void {
    if (this.sortBy === column) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = column;
      this.sortDir = 'asc';
    }
    this.load();
  }

  prevPage(): void {
    if (this.page === 0) {
      return;
    }
    this.page -= 1;
    this.load();
  }

  nextPage(): void {
    if (this.page + 1 >= this.totalPages) {
      return;
    }
    this.page += 1;
    this.load();
  }

  onDateChange(value: string): void {
    this.date = value;
    if (!this.date) {
      this.availability = '';
    }
  }

  startEdit(room: AdminRoomItem): void {
    this.selected = room;
    const normalizedBedType = room.bedType || 'Queen';
    const normalizedRoomStatus = room.roomStatus || 'AVAILABLE';
    this.editForm = {
      bedType: normalizedBedType,
      pricePerNight: room.pricePerNight,
      roomStatus: normalizedRoomStatus,
      amenitiesCsv: room.amenitiesCsv || '',
      occupancyAdults: room.occupancyAdults,
      occupancyChildren: room.occupancyChildren
    };
    this.selectedAmenities = (room.amenitiesCsv || '')
      .split(',')
      .map((value) => value.trim())
      .filter((value) => this.allowedAmenities.includes(value));
    this.successMessage = '';
    this.errorMessage = '';
    this.editErrorMessage = '';
  }

  openAddRoom(): void {
    this.addMode = true;
    this.addSubmitted = false;
    this.addSuccessMessage = '';
    this.addGeneratedRoomCode = '';
    this.addForm = {
      roomType: 'Standard',
      bedType: 'Queen',
      pricePerNight: 1000,
      amenitiesCsv: '',
      availability: 'Available',
      occupancyAdults: 1,
      occupancyChildren: 0,
      description: ''
    };
    this.addAmenities = [];
    this.addTouched = {
      pricePerNight: false,
      occupancyAdults: false,
      occupancyChildren: false,
      description: false
    };
  }

  closeAddRoom(): void {
    this.addMode = false;
  }

  submitAddRoom(): void {
    this.addSubmitted = true;
    if (!this.isAddFormValid) {
      return;
    }
    this.addForm.amenitiesCsv = this.addAmenities.join(', ');
    this.adminService.addRoom(this.addForm).subscribe({
      next: (room) => {
        this.addSuccessMessage = `Room added successfully: ${room.roomCode} | ${room.roomType} | INR ${room.pricePerNight}`;
        this.addGeneratedRoomCode = room.roomCode;
        this.load(true);
      },
      error: (error) => {
        this.errorMessage = error?.error?.error ?? 'Unable to add room.';
      }
    });
  }

  addAnotherRoom(): void {
    this.openAddRoom();
  }

  returnToDashboard(): void {
    this.router.navigateByUrl('/admin/home');
  }

  toggleAddAmenity(amenity: string, checked: boolean): void {
    if (checked) {
      if (!this.addAmenities.includes(amenity)) {
        this.addAmenities = [...this.addAmenities, amenity];
      }
    } else {
      this.addAmenities = this.addAmenities.filter((value) => value !== amenity);
    }
  }

  isAddAmenitySelected(amenity: string): boolean {
    return this.addAmenities.includes(amenity);
  }

  onBulkFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.bulkFile = input.files && input.files.length > 0 ? input.files[0] : null;
  }

  downloadTemplate(): void {
    this.adminService.downloadRoomTemplate().subscribe({
      next: (response) => {
        const blob = response.body;
        if (!blob) {
          this.errorMessage = 'Unable to download template.';
          return;
        }
        const disposition = response.headers.get('content-disposition') || '';
        const fileNameMatch = disposition.match(/filename="?([^"]+)"?/i);
        const fileName = fileNameMatch?.[1] || 'room-import-template.csv';
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
      },
      error: (error) => {
        this.errorMessage = error?.error?.error ?? 'Unable to download template.';
      }
    });
  }

  bulkUpload(): void {
    if (!this.bulkFile) {
      this.errorMessage = 'Please select a CSV file.';
      return;
    }
    this.adminService.bulkUploadRooms(this.bulkFile).subscribe({
      next: (res) => {
        this.successMessage = res.message;
        this.bulkFile = null;
        this.load(true);
      },
      error: (error) => {
        this.errorMessage = error?.error?.error ?? 'Bulk upload failed.';
      }
    });
  }

  cancelEdit(): void {
    this.selected = null;
    this.showConfirmModal = false;
    this.editErrorMessage = '';
  }

  saveEdit(): void {
    if (!this.selected) {
      return;
    }
    if (!this.isEditFormValid) {
      this.editErrorMessage = 'Please fix validation errors in edit form.';
      return;
    }
    this.editErrorMessage = '';
    this.showConfirmModal = true;
  }

  closeConfirmModal(): void {
    this.showConfirmModal = false;
  }

  confirmSaveEdit(): void {
    if (!this.selected) {
      return;
    }
    if (!this.isEditFormValid) {
      this.editErrorMessage = 'Please fix validation errors in edit form.';
      this.showConfirmModal = false;
      return;
    }
    if (this.selectedAmenities.length === 0) {
      this.editErrorMessage = 'Select at least one amenity.';
      this.showConfirmModal = false;
      return;
    }
    this.editForm.amenitiesCsv = this.selectedAmenities.join(', ');
    this.updating = true;
    this.adminService.updateRoom(this.selected.roomCode, this.editForm).subscribe({
      next: () => {
        this.successMessage = `Room ${this.selected?.roomCode} details are updated successfully.`;
        this.updating = false;
        this.showConfirmModal = false;
        this.selected = null;
        this.editErrorMessage = '';
        this.load();
      },
      error: (error) => {
        this.updating = false;
        this.editErrorMessage = error?.error?.error ?? 'Unable to update room.';
      }
    });
  }

  toggleAmenity(amenity: string, checked: boolean): void {
    if (checked) {
      if (!this.selectedAmenities.includes(amenity)) {
        this.selectedAmenities = [...this.selectedAmenities, amenity];
      }
      return;
    }
    this.selectedAmenities = this.selectedAmenities.filter((value) => value !== amenity);
  }

  isAmenitySelected(amenity: string): boolean {
    return this.selectedAmenities.includes(amenity);
  }

  blockNegativeInput(event: KeyboardEvent): void {
    if (event.key === '-' || event.key === '+' || event.key.toLowerCase() === 'e') {
      event.preventDefault();
    }
  }

  blockNonNumericPaste(event: ClipboardEvent): void {
    const pasted = event.clipboardData?.getData('text') ?? '';
    if (!/^\d+$/.test(pasted.trim())) {
      event.preventDefault();
    }
  }

  sanitizeFilterNumber(field: 'priceMin' | 'priceMax' | 'maxOccupancy'): void {
    const value = this[field];
    if (value === null || value === undefined) {
      this[field] = undefined;
      return;
    }
    const numeric = Number(value);
    if (!Number.isFinite(numeric)) {
      this[field] = undefined;
      return;
    }
    this[field] = Math.max(0, Math.trunc(numeric));
  }

  touchAddField(field: keyof typeof this.addTouched): void {
    this.addTouched[field] = true;
  }

  sanitizeAddNumber(field: 'pricePerNight' | 'occupancyAdults' | 'occupancyChildren'): void {
    const value = Number(this.addForm[field]);
    if (!Number.isFinite(value)) {
      return;
    }
    this.addForm[field] = Math.max(0, Math.trunc(value));
    this.touchAddField(field);
  }

  sanitizeNumber(field: 'pricePerNight' | 'occupancyAdults' | 'occupancyChildren'): void {
    const value = Number(this.editForm[field]);
    if (!Number.isFinite(value)) {
      return;
    }
    this.editForm[field] = Math.max(0, Math.trunc(value));
  }

  get hasSelectedDate(): boolean {
    return !!this.date;
  }

  availabilityLabel(room: AdminRoomItem): string {
    if (!this.hasSelectedDate) {
      return 'Select date';
    }
    return room.availabilityStatus === 'AVAILABLE' ? 'Available' : 'Not Available';
  }

  get priceInvalid(): boolean {
    return !Number.isFinite(this.editForm.pricePerNight) || this.editForm.pricePerNight < 1000;
  }

  get adultsInvalid(): boolean {
    return !Number.isFinite(this.editForm.occupancyAdults)
      || this.editForm.occupancyAdults < 1
      || this.editForm.occupancyAdults > 10;
  }

  get childrenInvalid(): boolean {
    return !Number.isFinite(this.editForm.occupancyChildren)
      || this.editForm.occupancyChildren < 0
      || this.editForm.occupancyChildren > 5;
  }

  get isEditFormValid(): boolean {
    return !this.priceInvalid && !this.adultsInvalid && !this.childrenInvalid;
  }

  get addPriceInvalid(): boolean {
    return !Number.isFinite(this.addForm.pricePerNight) || this.addForm.pricePerNight < 1000;
  }

  get addAdultsInvalid(): boolean {
    return !Number.isFinite(this.addForm.occupancyAdults)
      || this.addForm.occupancyAdults < 1
      || this.addForm.occupancyAdults > 10;
  }

  get addChildrenInvalid(): boolean {
    return !Number.isFinite(this.addForm.occupancyChildren)
      || this.addForm.occupancyChildren < 0
      || this.addForm.occupancyChildren > 5;
  }

  get addTotalOccupancyInvalid(): boolean {
    return (Number(this.addForm.occupancyAdults) + Number(this.addForm.occupancyChildren)) > 10;
  }

  get addDescriptionInvalid(): boolean {
    return this.addForm.description.length > 500;
  }

  get showAddPriceError(): boolean {
    return this.addTouched.pricePerNight || this.addSubmitted;
  }

  get showAddAdultsError(): boolean {
    return this.addTouched.occupancyAdults || this.addSubmitted;
  }

  get showAddChildrenError(): boolean {
    return this.addTouched.occupancyChildren || this.addSubmitted;
  }

  get isAddFormValid(): boolean {
    return !!this.addForm.roomType
      && !this.addPriceInvalid
      && !this.addAdultsInvalid
      && !this.addChildrenInvalid
      && !this.addTotalOccupancyInvalid
      && !this.addDescriptionInvalid
      && !!this.addForm.availability;
  }
}
