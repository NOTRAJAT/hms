import { Injectable } from '@angular/core';
import { jsPDF } from 'jspdf';
import { InvoiceResponse } from '../models/booking.model';

@Injectable({ providedIn: 'root' })
export class InvoiceService {
  private readonly hotelName = 'Renaissance Stay';
  private readonly hotelTagline = 'bringing revolution to reservations';
  private readonly hotelAddress = '12 Garden Lane, City Center';
  private readonly hotelEmail = 'support@renaissancestay.example';
  private readonly hotelPhone = '+91 90000 12345';
  private readonly hotelLogoLabel = 'RS';

  downloadInvoice(record: InvoiceResponse): void {
    const doc = new jsPDF();
    const line = (y: number, color: [number, number, number] = [210, 184, 144]) => {
      doc.setDrawColor(...color);
      doc.line(14, y, 196, y);
    };
    const printedAt = this.formatDateTime(record.invoiceDateTime);
    const hotelName = this.hotelName;
    const hotelAddress = this.hotelAddress;
    const hotelEmail = this.hotelEmail;
    const hotelPhone = this.hotelPhone;

    doc.setFillColor(246, 238, 226);
    doc.roundedRect(10, 8, 190, 32, 4, 4, 'F');
    doc.setDrawColor(176, 132, 75);
    doc.roundedRect(14, 12, 18, 18, 3, 3, 'S');
    doc.setFillColor(245, 233, 214);
    doc.roundedRect(14, 12, 18, 18, 3, 3, 'F');
    doc.setFont('times', 'bold');
    doc.setFontSize(11);
    doc.text(this.hotelLogoLabel, 23, 23, { align: 'center' });

    doc.setTextColor(76, 47, 33);
    doc.setFontSize(18);
    doc.text(hotelName, 38, 20);
    doc.setFont('times', 'italic');
    doc.setFontSize(10);
    doc.text(this.hotelTagline, 38, 26);
    doc.setFont('times', 'normal');
    doc.setFontSize(10);
    doc.text(hotelAddress, 38, 32);
    doc.text(`${hotelEmail} | ${hotelPhone}`, 38, 37);
    doc.setTextColor(0, 0, 0);
    line(42);

    doc.setFontSize(12);
    doc.setFont('times', 'bold');
    doc.text(`Invoice #: ${record.invoiceId}`, 14, 50);
    doc.setFont('times', 'normal');
    doc.text(`Booking ID: ${record.bookingId}`, 14, 56);
    doc.text(`Transaction ID: ${record.transactionId}`, 14, 62);
    doc.text(`Invoice Date: ${printedAt}`, 14, 68);

    line(72);
    doc.setFontSize(11);
    doc.setFont('times', 'bold');
    doc.text('Customer Details', 14, 80);
    doc.setFont('times', 'normal');
    doc.setFontSize(10);
    doc.text(`Name: ${record.customerName}`, 14, 86);
    doc.text(`Email: ${record.customerEmail}`, 14, 91);
    doc.text(`Mobile: ${record.customerMobile}`, 14, 96);

    line(100);
    doc.setFontSize(11);
    doc.setFont('times', 'bold');
    doc.text('Booking Details', 14, 108);
    doc.setFont('times', 'normal');
    doc.setFontSize(10);
    doc.text(`Room Type: ${record.roomType}`, 14, 114);
    doc.text(`Occupancy Limit: ${record.occupancyAdults} adults, ${record.occupancyChildren} children`, 14, 119);
    doc.text(`Price per Night: INR ${record.pricePerNight}`, 14, 124);
    doc.text(`Check-in: ${record.checkInDate}`, 14, 129);
    doc.text(`Check-out: ${record.checkOutDate}`, 14, 134);
    doc.text(`Nights: ${record.nights}`, 14, 139);
    doc.text(`Guests: ${record.adults} adults, ${record.children} children`, 14, 144);

    line(148);
    doc.setFontSize(11);
    doc.setFont('times', 'bold');
    doc.text('Charges', 14, 156);
    doc.setFont('times', 'normal');
    doc.setFontSize(10);
    doc.text(`Base Price: INR ${record.basePrice}`, 14, 162);
    doc.text(`GST (10%): INR ${record.gstAmount}`, 14, 167);
    doc.text(`Service Charge (2%): INR ${record.serviceChargeAmount}`, 14, 172);
    doc.setFont('times', 'bold');
    doc.text(`Total Paid: INR ${record.totalAmount}`, 14, 178);
    doc.setFont('times', 'normal');
    doc.text(`Payment Method: ${record.paymentMethod}`, 14, 183);

    line(188, [176, 132, 75]);
    doc.setFontSize(10);
    doc.setFont('times', 'italic');
    doc.text(`Thank you for choosing ${hotelName}.`, 14, 196);

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
