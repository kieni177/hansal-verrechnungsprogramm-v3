import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../models/product.model';
import { BaseService } from '../shared/base/base.service';

@Injectable({
  providedIn: 'root'
})
export class ProductService extends BaseService<Product> {
  protected resourcePath = 'products';

  constructor(http: HttpClient) {
    super(http);
  }

  search(name: string): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}/search`, { params: { name } });
  }

  initializeDefaults(): Observable<Product[]> {
    return this.http.post<Product[]>(`${this.apiUrl}/init-defaults`, {});
  }
}
