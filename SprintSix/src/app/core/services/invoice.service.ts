import { Injectable } from '@angular/core';
import { jsPDF } from 'jspdf';
import { InvoiceResponse, InvoiceServiceChargeDetail } from '../models/booking.model';

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
    const pageTop = 14;
    const pageBottom = 285;
    let y = 0;
    const line = (lineY: number, color: [number, number, number] = [210, 184, 144]) => {
      doc.setDrawColor(...color);
      doc.line(14, lineY, 196, lineY);
    };
    const ensureSpace = (required: number) => {
      if (y + required <= pageBottom) {
        return;
      }
      doc.addPage();
      y = pageTop;
    };
    const write = (text: string, fontSize = 10, style: 'normal' | 'bold' | 'italic' = 'normal', gap = 6) => {
      ensureSpace(gap);
      doc.setFont('times', style);
      doc.setFontSize(fontSize);
      doc.text(text, 14, y);
      y += gap;
    };
    const writeWrapped = (text: string, fontSize = 10, style: 'normal' | 'bold' | 'italic' = 'normal', width = 178, gap = 5) => {
      const lines = doc.splitTextToSize(text, width);
      const required = Math.max(lines.length * gap + 1, gap);
      ensureSpace(required);
      doc.setFont('times', style);
      doc.setFontSize(fontSize);
      doc.text(lines, 14, y);
      y += lines.length * gap + 1;
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
    y = 50;

    write(`Invoice #: ${record.invoiceId}`, 12, 'bold');
    write(`Booking ID: ${record.bookingId}`);
    write(`Transaction ID: ${record.transactionId}`);
    write(`Invoice Date: ${printedAt}`);

    y += 2;
    line(y);
    y += 8;
    write('Customer Details', 11, 'bold');
    write(`Name: ${record.customerName}`);
    write(`Email: ${record.customerEmail}`);
    write(`Mobile: ${record.customerMobile}`);

    y += 2;
    line(y);
    y += 8;
    write('Booking Details', 11, 'bold');
    write(`Room Type: ${record.roomType}`);
    write(`Occupancy Limit: ${record.occupancyAdults} adults, ${record.occupancyChildren} children`);
    write(`Price per Night: INR ${record.pricePerNight}`);
    write(`Check-in: ${record.checkInDate}`);
    write(`Check-out: ${record.checkOutDate}`);
    write(`Nights: ${record.nights}`);
    write(`Guests: ${record.adults} adults, ${record.children} children`);

    y += 2;
    line(y);
    y += 8;
    write('Charges', 11, 'bold');
    write(`Base Price: INR ${record.basePrice}`);
    write(`GST (10%): INR ${record.gstAmount}`);
    write(`Service Charge (2%): INR ${record.serviceChargeAmount}`);
    write(`Service Billing (${record.additionalServiceCount}): INR ${record.additionalServiceAmount}`);
    write(`Refund Initiated (Services): INR ${record.serviceRefundInitiatedAmount}`);
    write(`Room Booking Total: INR ${record.totalAmount}`, 10, 'bold');
    write(`Grand Total (Before Refund): INR ${record.grandTotalAmount}`, 10, 'bold');
    write(`Net Payable (After Initiated Refund): INR ${record.netPayableAmount}`, 10, 'bold');
    write(`Payment Method: ${record.paymentMethod}`);

    this.writeServiceChargeDetails(record.serviceChargeDetails, () => y, (nextY) => { y = nextY; }, ensureSpace, line, write, writeWrapped);

    y += 2;
    ensureSpace(12);
    line(y, [176, 132, 75]);
    y += 8;
    write(`Thank you for choosing ${hotelName}.`, 10, 'italic');

    doc.save(`invoice-${record.invoiceId}.pdf`);
  }

  private writeServiceChargeDetails(
      lines: InvoiceServiceChargeDetail[],
      getY: () => number,
      setY: (value: number) => void,
      ensureSpace: (required: number) => void,
      line: (y: number, color?: [number, number, number]) => void,
      write: (text: string, fontSize?: number, style?: 'normal' | 'bold' | 'italic', gap?: number) => void,
      writeWrapped: (text: string, fontSize?: number, style?: 'normal' | 'bold' | 'italic', width?: number, gap?: number) => void
  ): void {
    const serviceLines = lines ?? [];
    let y = getY();
    y += 2;
    line(y);
    y += 8;
    setY(y);
    write('Service Charge Details', 11, 'bold');

    if (!serviceLines.length) {
      write('No additional service transactions linked to this booking.');
      return;
    }

    serviceLines.forEach((item, index) => {
      ensureSpace(16);
      line(getY(), [235, 223, 204]);
      setY(getY() + 6);
      write(`${index + 1}. ${item.serviceType} · ${item.serviceSummary}`, 10, 'bold');
      write(`Request: ${item.requestId} · Status: ${item.status} · Amount: INR ${item.amount}`);
      write(`Selected Date/Time: ${item.serviceDateTime}`);
      writeWrapped(`Selected Details: ${item.serviceDetails}`);
      if (item.refundInitiatedAmount > 0) {
        writeWrapped(
            `Cancellation Note: ${item.refundNote || `Refund amount INR ${item.refundInitiatedAmount} initiated to bank and will be processed in 2 business days.`}`,
            10,
            'italic'
        );
      }
    });
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
