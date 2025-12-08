import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Slaughter } from '../models/slaughter.model';
import { BaseService } from '../shared/base/base.service';

@Injectable({
  providedIn: 'root'
})
export class SlaughterService extends BaseService<Slaughter> {
  protected resourcePath = 'slaughters';

  constructor(http: HttpClient) {
    super(http);
  }

  searchByCowTag(cowTag: string): Observable<Slaughter[]> {
    return this.http.get<Slaughter[]>(`${this.apiUrl}/search`, { params: { cowTag } });
  }

  getByDateRange(startDate: string, endDate: string): Observable<Slaughter[]> {
    return this.http.get<Slaughter[]>(`${this.apiUrl}/date-range`, {
      params: { startDate, endDate }
    });
  }
}
