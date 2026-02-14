import { Injectable } from '@angular/core';
import { jsPDF } from 'jspdf';
import { InvoiceResponse } from '../models/booking.model';

@Injectable({ providedIn: 'root' })
export class InvoiceService {
  private readonly hotelName = 'Hotel Management System';
  private readonly hotelAddress = '12 Garden Lane, City Center';
  private readonly hotelEmail = 'support@hotel.example';
  private readonly hotelPhone = '+91 90000 12345';
  private readonly hotelLogoLabel = 'HMS';

  downloadInvoice(record: InvoiceResponse): void {
    const doc = new jsPDF();
    const line = (y: number) => doc.line(14, y, 196, y);
    const printedAt = this.formatDateTime(record.invoiceDateTime);

    doc.setDrawColor(176, 132, 75);
    doc.setFillColor(245, 233, 214);
    doc.roundedRect(14, 10, 18, 18, 3, 3, 'FD');
    doc.setFont('times', 'bold');
    doc.setFontSize(11);
    doc.text(this.hotelLogoLabel, 23, 21, { align: 'center' });

    doc.setFontSize(16);
    doc.text(record.hotelName || this.hotelName, 38, 17);
    doc.setFont('times', 'normal');
    doc.setFontSize(10);
    doc.text(record.hotelAddress || this.hotelAddress, 38, 23);
    doc.text(`${record.hotelEmail || this.hotelEmail} | ${record.hotelSupportNumber || this.hotelPhone}`, 38, 28);
    line(30);

    doc.setFontSize(12);
    doc.text(`Invoice #: ${record.invoiceId}`, 14, 38);
    doc.text(`Booking ID: ${record.bookingId}`, 14, 44);
    doc.text(`Transaction ID: ${record.transactionId}`, 14, 50);
    doc.text(`Invoice Date: ${printedAt}`, 14, 56);

    line(60);
    doc.setFontSize(11);
    doc.text('Customer Details', 14, 68);
    doc.setFontSize(10);
    doc.text(`Name: ${record.customerName}`, 14, 74);
    doc.text(`Email: ${record.customerEmail}`, 14, 79);
    doc.text(`Mobile: ${record.customerMobile}`, 14, 84);

    line(88);
    doc.setFontSize(11);
    doc.text('Booking Details', 14, 96);
    doc.setFontSize(10);
    doc.text(`Room Type: ${record.roomType}`, 14, 102);
    doc.text(`Occupancy Limit: ${record.occupancyAdults} adults, ${record.occupancyChildren} children`, 14, 107);
    doc.text(`Price per Night: INR ${record.pricePerNight}`, 14, 112);
    doc.text(`Check-in: ${record.checkInDate}`, 14, 117);
    doc.text(`Check-out: ${record.checkOutDate}`, 14, 122);
    doc.text(`Nights: ${record.nights}`, 14, 127);
    doc.text(`Guests: ${record.adults} adults, ${record.children} children`, 14, 132);

    line(136);
    doc.setFontSize(11);
    doc.text('Charges', 14, 144);
    doc.setFontSize(10);
    doc.text(`Base Price: INR ${record.basePrice}`, 14, 150);
    doc.text(`GST (10%): INR ${record.gstAmount}`, 14, 155);
    doc.text(`Service Charge (2%): INR ${record.serviceChargeAmount}`, 14, 160);
    doc.text(`Total Paid: INR ${record.totalAmount}`, 14, 166);
    doc.text(`Payment Method: ${record.paymentMethod}`, 14, 171);

    line(176);
    doc.setFontSize(10);
    doc.text('Thank you for choosing us.', 14, 184);

    doc.save(`invoice-${record.invoiceId}.pdf`);
  }

  private formatDateTime(value: string): string {
    if (!value) {
      return '';
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return value;
    }
    const dd = String(date.getDate()).padStart(2, '0');
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const yyyy = date.getFullYear();
    const hh = String(date.getHours()).padStart(2, '0');
    const min = String(date.getMinutes()).padStart(2, '0');
    const sec = String(date.getSeconds()).padStart(2, '0');
    return `${dd}-${mm}-${yyyy} ${hh}:${min}:${sec}`;
  }
}
