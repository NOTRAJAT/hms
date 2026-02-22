import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, PLATFORM_ID, ViewChild, inject } from '@angular/core';
import { NgFor, NgIf, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../core/services/admin.service';
import { AdminDashboardSummary, AdminRoomOccupancyGridResponse } from '../../core/models/admin.model';
import { Chart, ChartConfiguration, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [NgIf, NgFor, FormsModule],
  templateUrl: './admin-reports.component.html'
})
export class AdminReportsComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('trendCanvas') trendCanvas?: ElementRef<HTMLCanvasElement>;

  private readonly platformId = inject(PLATFORM_ID);
  private trendChart?: Chart;
  private viewReady = false;
  private renderScheduled = false;
  private trendRenderAttempts = 0;

  summary: AdminDashboardSummary | null = null;
  selectedRoomType: 'Standard' | 'Deluxe' | 'Suite' | 'Supreme' = 'Standard';
  occupancyGrid: AdminRoomOccupancyGridResponse | null = null;
  roomPage = 0;
  readonly roomPageSize = 2;
  hoveredRoomCode = '';
  hoveredDateIndex: number | null = null;
  hoveredDate = '';
  hoveredStatus: 'Booked' | 'Free' | '' = '';

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.adminService.dashboardSummary().subscribe({
      next: (data) => {
        this.summary = data;
        this.scheduleRender();
      }
    });
    this.loadOccupancyGrid();
  }

  ngAfterViewInit(): void {
    this.viewReady = true;
    this.scheduleRender();
  }

  ngOnDestroy(): void {
    this.trendChart?.destroy();
  }

  onRoomTypeChange(): void {
    this.roomPage = 0;
    this.loadOccupancyGrid();
  }

  prevRooms(): void {
    if (this.roomPage > 0) {
      this.roomPage -= 1;
      this.loadOccupancyGrid();
    }
  }

  nextRooms(): void {
    if (!this.occupancyGrid) {
      return;
    }
    if (this.roomPage + 1 < this.occupancyGrid.totalPages) {
      this.roomPage += 1;
      this.loadOccupancyGrid();
    }
  }

  trackByDate(_: number, value: string): string {
    return value;
  }

  showDateLabel(index: number, total: number): boolean {
    return index === 0 || index === total - 1 || index % 3 === 0;
  }

  onOccupancyHover(roomCode: string, dateIndex: number, date: string, occupied: boolean): void {
    this.hoveredRoomCode = roomCode;
    this.hoveredDateIndex = dateIndex;
    this.hoveredDate = date;
    this.hoveredStatus = occupied ? 'Booked' : 'Free';
  }

  clearOccupancyHover(): void {
    this.hoveredRoomCode = '';
    this.hoveredDateIndex = null;
    this.hoveredDate = '';
    this.hoveredStatus = '';
  }

  onRowHover(event: MouseEvent, roomCode: string, rowOccupancy: boolean[], dates: string[]): void {
    const host = event.currentTarget as HTMLElement | null;
    if (!host || !rowOccupancy.length || !dates.length) {
      return;
    }
    const bar = host.hasAttribute('data-occupancy-bar')
      ? host
      : (host.querySelector('[data-occupancy-bar]') as HTMLElement | null);
    if (!bar) {
      return;
    }
    const rect = bar.getBoundingClientRect();
    const relativeX = Math.min(Math.max(event.clientX - rect.left, 0), rect.width - 1);
    const slotWidth = rect.width / rowOccupancy.length;
    const rawIndex = Math.floor(relativeX / slotWidth);
    const index = Math.min(Math.max(rawIndex, 0), rowOccupancy.length - 1);
    this.onOccupancyHover(roomCode, index, dates[index], rowOccupancy[index]);
  }

  private loadOccupancyGrid(): void {
    this.adminService.roomOccupancyGrid(this.selectedRoomType, this.roomPage, this.roomPageSize).subscribe({
      next: (data) => {
        this.occupancyGrid = data;
        if (data.rows.length > 0) {
          this.hoveredRoomCode = data.rows[0].roomCode;
        } else {
          this.hoveredRoomCode = '';
          this.clearOccupancyHover();
        }
      }
    });
  }

  private scheduleRender(): void {
    if (!this.summary || !this.viewReady || !isPlatformBrowser(this.platformId)) {
      return;
    }
    if (this.renderScheduled) {
      return;
    }
    this.renderScheduled = true;
    setTimeout(() => {
      this.renderScheduled = false;
      this.renderTrend();
    }, 0);
  }

  private renderTrend(): void {
    if (!this.summary) {
      return;
    }
    if (!this.trendCanvas?.nativeElement) {
      if (this.trendRenderAttempts < 5) {
        this.trendRenderAttempts += 1;
        setTimeout(() => this.renderTrend(), 0);
      }
      return;
    }
    this.trendRenderAttempts = 0;
    this.trendChart?.destroy();
    const ctx = this.trendCanvas.nativeElement.getContext('2d');
    if (!ctx) {
      return;
    }
    const trendFill = ctx.createLinearGradient(0, 0, 0, 280);
    trendFill.addColorStop(0, 'rgba(217, 119, 6, 0.38)');
    trendFill.addColorStop(1, 'rgba(251, 191, 36, 0.03)');

    const trendConfig: ChartConfiguration<'line'> = {
      type: 'line',
      data: {
        labels: ['Daily', 'Weekly', 'Monthly'],
        datasets: [{
          label: 'Bookings',
          data: [this.summary.dailyBookings, this.summary.weeklyBookings, this.summary.monthlyBookings],
          borderColor: '#9a3412',
          backgroundColor: trendFill,
          pointBackgroundColor: '#7c2d12',
          pointBorderColor: '#fde68a',
          pointBorderWidth: 1.5,
          pointRadius: 5,
          pointHoverRadius: 7,
          fill: true,
          tension: 0.42
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: 'rgba(41, 31, 26, 0.95)',
            titleColor: '#fde68a',
            bodyColor: '#fff7ed',
            padding: 10
          }
        },
        scales: {
          x: {
            grid: { display: false },
            ticks: { color: '#7c5f50', font: { weight: 600 } }
          },
          y: {
            beginAtZero: true,
            ticks: { precision: 0, color: '#8a6b5b' },
            grid: { color: 'rgba(120, 93, 78, 0.15)' }
          }
        }
      }
    };
    this.trendChart = new Chart(this.trendCanvas.nativeElement, trendConfig);
  }
}
