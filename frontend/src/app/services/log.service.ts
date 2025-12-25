import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, interval, switchMap, startWith } from 'rxjs';
import { environment } from '../../environments/environment';

export interface LogEntry {
  timestamp: string;
  level: string;
  logger: string;
  message: string;
  thread: string;
}

@Injectable({
  providedIn: 'root'
})
export class LogService {
  private apiUrl = `${environment.apiUrl}/logs`;

  constructor(private http: HttpClient) {}

  getRecentLogs(limit: number = 100): Observable<LogEntry[]> {
    return this.http.get<LogEntry[]>(`${this.apiUrl}?limit=${limit}`);
  }

  getLogsByLevel(level: string, limit: number = 100): Observable<LogEntry[]> {
    return this.http.get<LogEntry[]>(`${this.apiUrl}/level/${level}?limit=${limit}`);
  }

  getLogsSince(timestamp: string): Observable<LogEntry[]> {
    return this.http.get<LogEntry[]>(`${this.apiUrl}/since?timestamp=${timestamp}`);
  }

  getLogCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.apiUrl}/count`);
  }

  clearLogs(): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(this.apiUrl);
  }

  // Poll for new logs every X seconds
  pollLogs(intervalMs: number = 3000, limit: number = 100): Observable<LogEntry[]> {
    return interval(intervalMs).pipe(
      startWith(0),
      switchMap(() => this.getRecentLogs(limit))
    );
  }
}
