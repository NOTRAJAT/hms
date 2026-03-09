import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, PLATFORM_ID, ViewChild, inject } from '@angular/core';
import { NgIf } from '@angular/common';
import { AuthSessionService } from '../../core/services/auth-session.service';
import { AdminService } from '../../core/services/admin.service';
import { AdminDashboardSummary } from '../../core/models/admin.model';
import { Chart, ChartConfiguration, registerables } from 'chart.js';
import { isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';

Chart.register(...registerables);

@Component({
  selector: 'app-admin-home',
  standalone: true,
  imports: [NgIf, RouterLink],
  templateUrl: './admin-home.component.html'
})
export class AdminHomeComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('bookingsCanvas') bookingsCanvas?: ElementRef<HTMLCanvasElement>;

  private readonly platformId = inject(PLATFORM_ID);
  private bookingsChart?: Chart;
  private viewReady = false;
  private renderScheduled = false;

  summary: AdminDashboardSummary | null = null;

  constructor(
    public session: AuthSessionService,
    private adminService: AdminService
  ) {}

  ngOnInit(): void {
    this.adminService.dashboardSummary().subscribe({
      next: (data) => {
        this.summary = data;
        this.scheduleRender();
      }
    });
  }

  ngAfterViewInit(): void {
    this.viewReady = true;
    this.scheduleRender();
  }

  ngOnDestroy(): void {
    this.bookingsChart?.destroy();
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
      this.renderCharts();
    }, 0);
  }

  private renderCharts(): void {
    if (!this.summary || !this.viewReady || !isPlatformBrowser(this.platformId)) {
      return;
    }
    if (!this.bookingsCanvas?.nativeElement) {
      return;
    }

    this.bookingsChart?.destroy();

    const ctx = this.bookingsCanvas.nativeElement.getContext('2d');
    if (!ctx) {
      return;
    }
    const warmGradient = ctx.createLinearGradient(0, 0, 0, 320);
    warmGradient.addColorStop(0, 'rgba(180, 83, 9, 0.95)');
    warmGradient.addColorStop(1, 'rgba(245, 158, 11, 0.7)');

    const bookingConfig: ChartConfiguration<'bar'> = {
      type: 'bar',
      data: {
        labels: ['Daily', 'Weekly', 'Monthly'],
        datasets: [{
          data: [this.summary.dailyBookings, this.summary.weeklyBookings, this.summary.monthlyBookings],
          label: 'Bookings',
          borderRadius: 12,
          borderSkipped: false,
          backgroundColor: warmGradient,
          borderColor: '#92400e',
          borderWidth: 1.5,
          hoverBackgroundColor: '#b45309'
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
            padding: 10,
            displayColors: false
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

    this.bookingsChart = new Chart(this.bookingsCanvas.nativeElement, bookingConfig);
  }
}
