import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ResetDatabaseResponse {
  success: boolean;
  message: string;
  productsLoaded?: number;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  resetDatabase(): Observable<ResetDatabaseResponse> {
    return this.http.post<ResetDatabaseResponse>(`${this.apiUrl}/reset-database`, {});
  }
}
