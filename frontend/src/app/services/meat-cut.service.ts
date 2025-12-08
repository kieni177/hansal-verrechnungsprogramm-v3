import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface MeatCutAvailability {
  meatCutId: number;
  cowTag: string;
  cowId: string;
  slaughterDate: string;
  availableWeight: number;
  totalWeight: number;
  pricePerKg: number;
  productName: string;
}

@Injectable({
  providedIn: 'root'
})
export class MeatCutService {
  private apiUrl = `${environment.apiUrl}/meat-cuts`;

  constructor(private http: HttpClient) {}

  getAvailabilityByProduct(productId: number): Observable<MeatCutAvailability[]> {
    return this.http.get<MeatCutAvailability[]>(`${this.apiUrl}/availability/product/${productId}`);
  }
}
