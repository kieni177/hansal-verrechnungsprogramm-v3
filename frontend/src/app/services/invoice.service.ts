import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Invoice } from '../models/invoice.model';
import { BaseService } from '../shared/base/base.service';

@Injectable({
  providedIn: 'root'
})
export class InvoiceService extends BaseService<Invoice> {
  protected resourcePath = 'invoices';

  constructor(http: HttpClient) {
    super(http);
  }

  getByNumber(invoiceNumber: string): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.apiUrl}/number/${invoiceNumber}`);
  }

  getByOrderId(orderId: number): Observable<Invoice | null> {
    return this.http.get<Invoice>(`${this.apiUrl}/by-order/${orderId}`).pipe(
      catchError(error => {
        if (error.status === 404) {
          return of(null);
        }
        throw error;
      })
    );
  }

  createFromOrder(orderId: number): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.apiUrl}/from-order/${orderId}`, null);
  }

  downloadPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/pdf`, { responseType: 'blob' });
  }

  downloadCombinedPdf(ids: number[]): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/batch/pdf`, ids, { responseType: 'blob' });
  }
}
