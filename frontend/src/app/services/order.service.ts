import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order, OrderStatus } from '../models/order.model';
import { BaseService } from '../shared/base/base.service';

@Injectable({
  providedIn: 'root'
})
export class OrderService extends BaseService<Order> {
  protected resourcePath = 'orders';

  constructor(http: HttpClient) {
    super(http);
  }

  search(customerName: string): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/search`, { params: { customerName } });
  }

  getByStatus(status: OrderStatus): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/status/${status}`);
  }

  updateStatus(id: number, status: OrderStatus): Observable<Order> {
    return this.http.patch<Order>(`${this.apiUrl}/${id}/status`, null, { params: { status } });
  }
}
