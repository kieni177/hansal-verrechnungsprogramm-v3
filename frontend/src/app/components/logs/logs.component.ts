import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatBadgeModule } from '@angular/material/badge';
import { MatChipsModule } from '@angular/material/chips';
import { Subject, takeUntil, Subscription } from 'rxjs';

import { LogService, LogEntry } from '../../services/log.service';

@Component({
  selector: 'app-logs',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatFormFieldModule,
    MatSlideToggleModule,
    MatTooltipModule,
    MatBadgeModule,
    MatChipsModule
  ],
  templateUrl: './logs.component.html',
  styleUrls: ['./logs.component.scss']
})
export class LogsComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('logContainer') private logContainer!: ElementRef;

  private destroy$ = new Subject<void>();
  private pollSubscription?: Subscription;

  logs: LogEntry[] = [];
  filteredLogs: LogEntry[] = [];
  isAutoRefresh = true;
  selectedLevel = 'ALL';
  logLimit = 100;
  isLoading = false;
  autoScroll = true;
  private shouldScroll = false;

  levels = ['ALL', 'INFO', 'WARN', 'ERROR', 'DEBUG'];

  constructor(private logService: LogService) {}

  ngOnInit(): void {
    this.loadLogs();
    this.startAutoRefresh();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.stopAutoRefresh();
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll && this.autoScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  loadLogs(): void {
    this.isLoading = true;
    this.logService.getRecentLogs(this.logLimit)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (logs) => {
          this.logs = logs;
          this.applyFilter();
          this.isLoading = false;
          this.shouldScroll = true;
        },
        error: (error) => {
          console.error('Failed to load logs:', error);
          this.isLoading = false;
        }
      });
  }

  applyFilter(): void {
    if (this.selectedLevel === 'ALL') {
      this.filteredLogs = [...this.logs];
    } else {
      this.filteredLogs = this.logs.filter(log => log.level === this.selectedLevel);
    }
  }

  onLevelChange(): void {
    this.applyFilter();
  }

  toggleAutoRefresh(): void {
    if (this.isAutoRefresh) {
      this.startAutoRefresh();
    } else {
      this.stopAutoRefresh();
    }
  }

  private startAutoRefresh(): void {
    this.stopAutoRefresh();
    this.pollSubscription = this.logService.pollLogs(3000, this.logLimit)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (logs) => {
          this.logs = logs;
          this.applyFilter();
          if (this.autoScroll) {
            this.shouldScroll = true;
          }
        }
      });
  }

  private stopAutoRefresh(): void {
    if (this.pollSubscription) {
      this.pollSubscription.unsubscribe();
      this.pollSubscription = undefined;
    }
  }

  clearLogs(): void {
    this.logService.clearLogs()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.logs = [];
          this.filteredLogs = [];
        }
      });
  }

  refreshLogs(): void {
    this.loadLogs();
  }

  private scrollToBottom(): void {
    if (this.logContainer) {
      const el = this.logContainer.nativeElement;
      el.scrollTop = el.scrollHeight;
    }
  }

  scrollToTop(): void {
    if (this.logContainer) {
      this.logContainer.nativeElement.scrollTop = 0;
    }
  }

  getLevelClass(level: string): string {
    switch (level) {
      case 'ERROR': return 'log-error';
      case 'WARN': return 'log-warn';
      case 'INFO': return 'log-info';
      case 'DEBUG': return 'log-debug';
      default: return '';
    }
  }

  getLevelIcon(level: string): string {
    switch (level) {
      case 'ERROR': return 'error';
      case 'WARN': return 'warning';
      case 'INFO': return 'info';
      case 'DEBUG': return 'bug_report';
      default: return 'circle';
    }
  }

  formatTimestamp(timestamp: string): string {
    const date = new Date(timestamp);
    return date.toLocaleTimeString('de-AT', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }

  getLogCounts(): { info: number; warn: number; error: number } {
    return {
      info: this.logs.filter(l => l.level === 'INFO').length,
      warn: this.logs.filter(l => l.level === 'WARN').length,
      error: this.logs.filter(l => l.level === 'ERROR').length
    };
  }
}
