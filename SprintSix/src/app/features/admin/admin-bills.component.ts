import { Component, OnInit } from '@angular/core';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { jsPDF } from 'jspdf';
import {
  AdminBillCreatePayload,
  AdminBillItem,
  AdminBillServiceItem,
  AdminBillSummaryResponse,
  AdminBillUpdatePayload
} from '../../core/models/admin.model';
import { AdminService } from '../../core/services/admin.service';

@Component({
  selector: 'app-admin-bills',
  standalone: true,
  imports: [FormsModule, NgFor, NgIf, DatePipe],
  templateUrl: './admin-bills.component.html'
})
export class AdminBillsComponent implements OnInit {
  readonly serviceTypes = ['Spa', 'Dining', 'Room Service', 'Laundry', 'Transport', 'Other'];

  bills: AdminBillItem[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';

  q = '';
  paymentStatus: '' | 'PAID' | 'PENDING' = '';
  fromDate = '';
  toDate = '';
  sortBy = 'issueDate';
  sortDir: 'asc' | 'desc' = 'desc';
  page = 0;
  readonly size = 10;
  totalItems = 0;
  totalPages = 0;
  markPaying = '';
  downloadingCsv = false;
  summary: AdminBillSummaryResponse = {
    totalRevenue: 0,
    invoiceRevenue: 0,
    manualBillRevenue: 0,
    billRoomRevenue: 0,
    roomRevenue: 0,
    serviceRevenue: 0,
    otherRevenue: 0,
    taxRevenue: 0,
    discountTotal: 0,
    billCount: 0
  };
  descriptionTarget: AdminBillItem | null = null;

  createMode = false;
  createSubmitted = false;
  creating = false;
  customerIdDigits = '';
  customerSuggestions: Array<{ userId: string; name: string }> = [];
  customerLookupLoading = false;
  private customerLookupTimer: ReturnType<typeof setTimeout> | null = null;
  createForm: AdminBillCreatePayload = this.newBillPayload();

  editTarget: AdminBillItem | null = null;
  editSubmitted = false;
  updating = false;
  editForm: AdminBillUpdatePayload = {
    roomCharges: 0,
    additionalFees: 0,
    taxes: 0,
    discounts: 0,
    totalAmountDue: 0,
    serviceItems: []
  };

  ngOnInit(): void {
    this.load(true);
  }

  constructor(private adminService: AdminService) {}

  load(resetPage = false): void {
    if (resetPage) {
      this.page = 0;
    }
    if (this.invalidDateRange || this.invalidQuery) {
      return;
    }
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.adminService.bills({
      q: this.q.trim() || undefined,
      paymentStatus: this.paymentStatus || undefined,
      fromDate: this.fromDate || undefined,
      toDate: this.toDate || undefined,
      sortBy: this.sortBy,
      sortDir: this.sortDir,
      page: this.page,
      size: this.size
    }).subscribe({
      next: (response) => {
        this.bills = response.items;
        this.totalItems = response.totalItems;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error?.error?.error ?? 'Unable to fetch bills.';
      }
    });
    this.adminService.billSummary({
      q: this.q.trim() || undefined,
      paymentStatus: this.paymentStatus || undefined,
      fromDate: this.fromDate || undefined,
      toDate: this.toDate || undefined
    }).subscribe({
      next: (summary) => {
        this.summary = summary;
      },
      error: () => {
        this.summary = {
          totalRevenue: 0,
          invoiceRevenue: 0,
          manualBillRevenue: 0,
          billRoomRevenue: 0,
          roomRevenue: 0,
          serviceRevenue: 0,
          otherRevenue: 0,
          taxRevenue: 0,
          discountTotal: 0,
          billCount: 0
        };
      }
    });
  }

  onFromDateChange(value: string): void {
    this.fromDate = value;
    if (!this.toDate || this.toDate < this.fromDate) {
      this.toDate = this.fromDate;
    }
  }

  openCreate(): void {
    this.createMode = true;
    this.createSubmitted = false;
    this.customerIdDigits = '';
    this.customerSuggestions = [];
    this.createForm = this.newBillPayload();
    this.addServiceItem(this.createForm.serviceItems);
  }

  closeCreate(): void {
    this.createMode = false;
  }

  submitCreate(): void {
    this.createSubmitted = true;
    this.createForm.customerUserId = this.buildCustomerId(this.customerIdDigits);
    this.recalculateCreateTotal();
    if (!this.isCreateValid || this.creating) {
      return;
    }
    this.creating = true;
    this.adminService.createBill(this.createForm).subscribe({
      next: (bill) => {
        this.creating = false;
        this.successMessage = `Bill ${bill.billId} created successfully.`;
        this.createMode = false;
        this.load(true);
      },
      error: (error) => {
        this.creating = false;
        this.errorMessage = error?.error?.error ?? 'Unable to create bill.';
      }
    });
  }

  openEdit(bill: AdminBillItem): void {
    if (!bill.editable) {
      return;
    }
    this.editTarget = bill;
    this.editSubmitted = false;
    this.editForm = {
      roomCharges: bill.roomCharges,
      additionalFees: bill.additionalFees,
      taxes: bill.taxes,
      discounts: bill.discounts,
      totalAmountDue: bill.totalAmount,
      serviceItems: (bill.serviceItems || []).map((item) => ({ ...item }))
    };
    if (!this.editForm.serviceItems.length) {
      this.addServiceItem(this.editForm.serviceItems);
    }
  }

  closeEdit(): void {
    this.editTarget = null;
  }

  submitEdit(): void {
    if (!this.editTarget) {
      return;
    }
    this.editSubmitted = true;
    this.recalculateEditTotal();
    if (!this.isEditValid || this.updating) {
      return;
    }
    this.updating = true;
    this.adminService.updateBill(this.editTarget.billId, this.editForm).subscribe({
      next: () => {
        this.updating = false;
        this.successMessage = `Bill ${this.editTarget?.billId} updated successfully.`;
        this.editTarget = null;
        this.load();
      },
      error: (error) => {
        this.updating = false;
        this.errorMessage = error?.error?.error ?? 'Unable to update bill.';
      }
    });
  }

  addCreateService(): void {
    this.addServiceItem(this.createForm.serviceItems);
  }

  addEditService(): void {
    this.addServiceItem(this.editForm.serviceItems);
  }

  removeCreateService(index: number): void {
    this.createForm.serviceItems.splice(index, 1);
    this.recalculateCreateTotal();
  }

  removeEditService(index: number): void {
    this.editForm.serviceItems.splice(index, 1);
    this.recalculateEditTotal();
  }

  markAsPaid(bill: AdminBillItem): void {
    if (bill.paymentStatus === 'PAID' || this.markPaying === bill.billId) {
      return;
    }
    this.markPaying = bill.billId;
    this.errorMessage = '';
    this.successMessage = '';
    this.adminService.markBillPaid(bill.billId).subscribe({
      next: () => {
        this.markPaying = '';
        this.successMessage = `Bill ${bill.billId} marked as Paid.`;
        this.load();
      },
      error: (error) => {
        this.markPaying = '';
        this.errorMessage = error?.error?.error ?? 'Unable to update payment status.';
      }
    });
  }

  openDescription(bill: AdminBillItem): void {
    this.descriptionTarget = bill;
  }

  closeDescription(): void {
    this.descriptionTarget = null;
  }

  downloadBillPdf(bill: AdminBillItem): void {
    const doc = new jsPDF();
    let y = 16;
    const lineGap = 7;
    const services = bill.serviceItems || [];
    const issueDate = bill.issueDate ? new Date(bill.issueDate) : null;
    const issueDateText = issueDate && !Number.isNaN(issueDate.getTime())
      ? issueDate.toLocaleString('en-IN')
      : bill.issueDate;

    doc.setFontSize(16);
    doc.text('Hotel Bill', 14, y);
    y += 10;
    doc.setFontSize(11);
    doc.text(`Bill ID: ${bill.billId}`, 14, y);
    y += lineGap;
    doc.text(`Booking ID: ${bill.bookingId || '-'}`, 14, y);
    y += lineGap;
    doc.text(`Customer: ${bill.customerName} (${bill.customerUserId})`, 14, y);
    y += lineGap;
    doc.text(`Issue Date: ${issueDateText || '-'}`, 14, y);
    y += lineGap;
    doc.text(`Payment Status: ${bill.paymentStatus}`, 14, y);
    y += 10;

    doc.setFontSize(12);
    doc.text('Charges', 14, y);
    y += lineGap;
    doc.setFontSize(11);
    doc.text(`Room Charges: INR ${bill.roomCharges}`, 14, y);
    y += lineGap;
    doc.text(`Service Charges: INR ${bill.serviceCharges}`, 14, y);
    y += lineGap;
    doc.text(`Additional Charges: ${bill.additionalFees}${bill.editable ? '%' : ''}`, 14, y);
    y += lineGap;
    doc.text(`Tax: ${bill.taxes}${bill.editable ? '%' : ''}`, 14, y);
    y += lineGap;
    doc.text(`Discount: ${bill.discounts}${bill.editable ? '%' : ''}`, 14, y);
    y += lineGap;
    doc.text(`Total Amount: INR ${bill.totalAmount}`, 14, y);
    y += 10;

    doc.setFontSize(12);
    doc.text('Service Descriptions', 14, y);
    y += lineGap;
    doc.setFontSize(10);
    if (!services.length) {
      doc.text('No service items.', 14, y);
    } else {
      services.forEach((item, index) => {
        if (y > 276) {
          doc.addPage();
          y = 16;
        }
        const dateText = item.serviceDateTime ? new Date(item.serviceDateTime).toLocaleString('en-IN') : '-';
        const summary = `${index + 1}. ${item.serviceType} | ${dateText} | Qty ${item.quantity} | INR ${item.unitPrice}`;
        doc.text(summary, 14, y);
        y += 5;
        const description = (item.description || '').trim() || 'No description';
        const wrapped = doc.splitTextToSize(`Description: ${description}`, 180);
        doc.text(wrapped, 14, y);
        y += wrapped.length * 5 + 3;
      });
    }

    doc.save(`bill-${bill.billId}.pdf`);
  }

  downloadBillsCsv(): void {
    if (this.invalidDateRange || this.invalidQuery || this.downloadingCsv) {
      return;
    }
    this.downloadingCsv = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.adminService.downloadBillsCsv({
      q: this.q.trim() || undefined,
      paymentStatus: this.paymentStatus || undefined,
      fromDate: this.fromDate || undefined,
      toDate: this.toDate || undefined,
      sortBy: this.sortBy,
      sortDir: this.sortDir
    }).subscribe({
      next: (response) => {
        this.downloadingCsv = false;
        const blob = response.body;
        if (!blob) {
          this.errorMessage = 'CSV download failed.';
          return;
        }
        const filename = this.extractFilename(response.headers.get('content-disposition')) || 'bills-export.csv';
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = filename;
        anchor.click();
        URL.revokeObjectURL(url);
        this.successMessage = 'Bills CSV downloaded successfully.';
      },
      error: (error) => {
        this.downloadingCsv = false;
        this.errorMessage = error?.error?.error ?? 'Unable to download bills CSV.';
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
    if (this.page <= 0) {
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

  recalculateCreateTotal(): void {
    this.createForm.totalAmountDue = this.calculateTotal(this.createForm);
  }

  recalculateEditTotal(): void {
    this.editForm.totalAmountDue = this.calculateTotal(this.editForm);
  }

  sanitizeNumber(value: number | null | undefined): number {
    const num = Number(value ?? 0);
    if (!Number.isFinite(num) || num < 0) {
      return 0;
    }
    return Math.trunc(num);
  }

  allowOnlyNumberInput(event: KeyboardEvent): void {
    const allowedControlKeys = ['Backspace', 'Delete', 'ArrowLeft', 'ArrowRight', 'Tab', 'Home', 'End'];
    if (allowedControlKeys.includes(event.key)) {
      return;
    }
    if (!/^[0-9]$/.test(event.key)) {
      event.preventDefault();
    }
  }

  sanitizeServiceItem(item: AdminBillServiceItem): void {
    item.quantity = Math.max(1, this.sanitizeNumber(item.quantity));
    item.unitPrice = this.sanitizeNumber(item.unitPrice);
    item.taxPercent = Math.min(100, this.sanitizeNumber(item.taxPercent));
    item.discountPercent = Math.min(100, this.sanitizeNumber(item.discountPercent));
  }

  onCustomerDigitsChange(raw: string): void {
    const digits = String(raw ?? '').replace(/\D/g, '').slice(0, 6);
    this.customerIdDigits = digits;
    this.createForm.customerUserId = this.buildCustomerId(digits);

    if (this.customerLookupTimer) {
      clearTimeout(this.customerLookupTimer);
      this.customerLookupTimer = null;
    }
    if (!digits) {
      this.customerSuggestions = [];
      this.customerLookupLoading = false;
      return;
    }

    this.customerLookupLoading = true;
    this.customerLookupTimer = setTimeout(() => {
      this.adminService.users({
        q: this.createForm.customerUserId,
        role: 'CUSTOMER',
        status: 'ACTIVE',
        sortBy: 'userId',
        sortDir: 'asc',
        page: 0,
        size: 8
      }).subscribe({
        next: (response) => {
          const prefix = this.createForm.customerUserId.toUpperCase();
          this.customerSuggestions = (response.items || [])
            .filter((item) => item.userId.toUpperCase().startsWith(prefix))
            .map((item) => ({
              userId: item.userId,
              name: item.name
            }));
          this.customerLookupLoading = false;
        },
        error: () => {
          this.customerSuggestions = [];
          this.customerLookupLoading = false;
        }
      });
    }, 180);
  }

  selectCustomerSuggestion(userId: string): void {
    this.createForm.customerUserId = userId;
    this.customerIdDigits = userId.replace(/^CUST-/, '');
    this.customerSuggestions = [];
  }

  normalizeServiceItemField(
    item: AdminBillServiceItem,
    field: 'quantity' | 'unitPrice' | 'taxPercent' | 'discountPercent'
  ): void {
    if (field === 'quantity') {
      item.quantity = Math.max(1, this.sanitizeNumber(item.quantity));
      return;
    }
    if (field === 'unitPrice') {
      item.unitPrice = this.sanitizeNumber(item.unitPrice);
      return;
    }
    if (field === 'taxPercent') {
      item.taxPercent = Math.min(100, this.sanitizeNumber(item.taxPercent));
      return;
    }
    item.discountPercent = Math.min(100, this.sanitizeNumber(item.discountPercent));
  }

  get invalidDateRange(): boolean {
    return !!this.fromDate && !!this.toDate && this.fromDate > this.toDate;
  }

  get invalidQuery(): boolean {
    return !/^[a-zA-Z0-9\\s-]*$/.test(this.q);
  }

  get createCustomerInvalid(): boolean {
    return !/^CUST-\d{1,6}$/.test(this.createForm.customerUserId.trim());
  }

  get createRoomChargesInvalid(): boolean {
    return this.createForm.roomCharges == null || this.createForm.roomCharges < 0;
  }

  get createAdditionalFeesInvalid(): boolean {
    return this.createForm.additionalFees == null || this.createForm.additionalFees < 0 || this.createForm.additionalFees > 100;
  }

  get createPercentInvalid(): boolean {
    return this.createForm.taxes == null
      || this.createForm.discounts == null
      || this.createForm.taxes < 0
      || this.createForm.discounts < 0
      || this.sanitizeNumber(this.createForm.taxes) > 100
      || this.sanitizeNumber(this.createForm.discounts) > 100;
  }

  get createServiceItemsInvalid(): boolean {
    return this.createForm.serviceItems.some((item) =>
      !item.serviceType
      || !item.serviceDateTime
      || this.sanitizeNumber(item.quantity) < 1
      || this.sanitizeNumber(item.unitPrice) < 0
      || this.sanitizeNumber(item.taxPercent) < 0
      || this.sanitizeNumber(item.taxPercent) > 100
      || this.sanitizeNumber(item.discountPercent) < 0
      || this.sanitizeNumber(item.discountPercent) > 100
    );
  }

  get isCreateValid(): boolean {
    return !this.createCustomerInvalid
      && !this.createRoomChargesInvalid
      && !this.createAdditionalFeesInvalid
      && !this.createServiceItemsInvalid
      && !this.createPercentInvalid;
  }

  get isEditValid(): boolean {
    return !this.createServiceItemsInvalidFor(this.editForm.serviceItems)
      && this.sanitizeNumber(this.editForm.additionalFees) <= 100
      && this.sanitizeNumber(this.editForm.taxes) <= 100
      && this.sanitizeNumber(this.editForm.discounts) <= 100;
  }

  get editPercentInvalid(): boolean {
    return this.sanitizeNumber(this.editForm.taxes) > 100 || this.sanitizeNumber(this.editForm.discounts) > 100;
  }

  get editAdditionalFeesInvalid(): boolean {
    return this.editForm.additionalFees == null || this.editForm.additionalFees < 0 || this.editForm.additionalFees > 100;
  }

  private createServiceItemsInvalidFor(items: AdminBillServiceItem[]): boolean {
    return items.some((item) =>
      !item.serviceType
      || !item.serviceDateTime
      || this.sanitizeNumber(item.quantity) < 1
      || this.sanitizeNumber(item.unitPrice) < 0
      || this.sanitizeNumber(item.taxPercent) < 0
      || this.sanitizeNumber(item.taxPercent) > 100
      || this.sanitizeNumber(item.discountPercent) < 0
      || this.sanitizeNumber(item.discountPercent) > 100
    );
  }

  private newBillPayload(): AdminBillCreatePayload {
    return {
      customerUserId: '',
      roomCharges: 0,
      additionalFees: 0,
      taxes: 0,
      discounts: 0,
      totalAmountDue: 0,
      serviceItems: []
    };
  }

  private buildCustomerId(digits: string): string {
    const suffix = String(digits ?? '').replace(/\D/g, '').slice(0, 6);
    return suffix ? `CUST-${suffix}` : '';
  }

  private extractFilename(contentDisposition: string | null): string {
    if (!contentDisposition) {
      return '';
    }
    const match = /filename=\"?([^\";]+)\"?/i.exec(contentDisposition);
    return match?.[1] ?? '';
  }

  private addServiceItem(target: AdminBillServiceItem[]): void {
    target.push({
      serviceDateTime: '',
      serviceType: 'Room Service',
      description: '',
      quantity: 1,
      unitPrice: 0,
      taxPercent: 0,
      discountPercent: 0
    });
  }

  private calculateTotal(payload: AdminBillCreatePayload | AdminBillUpdatePayload): number {
    const room = this.sanitizeNumber(payload.roomCharges);
    const additionalPercent = this.sanitizeNumber(payload.additionalFees);
    const taxes = this.sanitizeNumber(payload.taxes);
    const discounts = this.sanitizeNumber(payload.discounts);
    const service = (payload.serviceItems || []).reduce((sum, item) => {
      this.sanitizeServiceItem(item);
      const base = item.quantity * item.unitPrice;
      const tax = Math.round(base * (item.taxPercent / 100));
      const discount = Math.round(base * (item.discountPercent / 100));
      return sum + base + tax - discount;
    }, 0);
    const baseSubtotal = room + service;
    const additionalAmount = Math.round(baseSubtotal * (Math.min(100, additionalPercent) / 100));
    const subtotal = baseSubtotal + additionalAmount;
    const taxAmount = Math.round(subtotal * (Math.min(100, taxes) / 100));
    const discountAmount = Math.round(subtotal * (Math.min(100, discounts) / 100));
    return subtotal + taxAmount - discountAmount;
  }
}
