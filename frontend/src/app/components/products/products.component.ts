import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { animate, state, style, transition, trigger } from '@angular/animations';
import { takeUntil } from 'rxjs';

import { BaseCrudComponent } from '../../shared/base/base-crud.component';
import { ProductService } from '../../services/product.service';
import { NotificationService } from '../../services/notification.service';
import { Product } from '../../models/product.model';
import { DialogUtils } from '../../shared/utils/dialog.utils';
import { API_MESSAGES } from '../../shared/constants/app.constants';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.scss'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({ height: '0px', minHeight: '0' })),
      state('expanded', style({ height: '*' })),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)'))
    ])
  ]
})
export class ProductsComponent extends BaseCrudComponent<Product> implements OnInit {
  searchTerm = '';
  displayedColumns = ['name', 'description', 'price', 'stockQuantity', 'actions'];
  dataSource = new MatTableDataSource<Product>([]);
  expandedElement: Product | null = null;
  editingElement: Product | null = null;
  editedProduct: Product | null = null;

  @ViewChild(MatSort) set sort(sort: MatSort) {
    if (sort) {
      this.dataSource.sort = sort;
    }
  }

  constructor(
    private productService: ProductService,
    private router: Router,
    private dialog: MatDialog,
    private notification: NotificationService
  ) {
    super();
  }

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.setLoading(true);
    this.productService.getAll()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (items) => {
          this.items = items;
          this.dataSource.data = items;
          this.setLoading(false);
        },
        error: (error) => {
          this.notification.error(API_MESSAGES.ERROR.LOAD);
          this.handleError(error);
        }
      });
  }

  applyFilter(): void {
    this.dataSource.filter = this.searchTerm.trim().toLowerCase();
  }

  add(): void {
    this.router.navigate(['/products/create']);
  }

  viewDetails(product: Product): void {
    this.router.navigate(['/products', product.id]);
  }

  edit(product: Product): void {
    this.router.navigate(['/products/edit', product.id]);
  }

  delete(id: number): void {
    DialogUtils.confirmAction(this.dialog)
      .pipe(takeUntil(this.destroy$))
      .subscribe(confirmed => {
        if (confirmed) this.remove(id);
      });
  }

  private remove(id: number): void {
    this.productService.delete(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notification.success(API_MESSAGES.SUCCESS.DELETE);
          this.loadAll();
        },
        error: (error) => {
          this.notification.error(API_MESSAGES.ERROR.DELETE);
          this.handleError(error);
        }
      });
  }

  toggleExpand(product: Product): void {
    if (this.editingElement === product) {
      return; // Don't collapse while editing
    }
    this.expandedElement = this.expandedElement === product ? null : product;
  }

  startEdit(product: Product): void {
    this.editingElement = product;
    this.editedProduct = { ...product };
    this.expandedElement = product; // Ensure row is expanded
  }

  cancelEdit(): void {
    this.editingElement = null;
    this.editedProduct = null;
  }

  saveEdit(): void {
    if (!this.editedProduct || !this.editedProduct.id) {
      return;
    }

    this.productService.update(this.editedProduct.id, this.editedProduct)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notification.success(API_MESSAGES.SUCCESS.UPDATE);
          this.editingElement = null;
          this.editedProduct = null;
          this.loadAll();
        },
        error: (error) => {
          this.notification.error(API_MESSAGES.ERROR.UPDATE);
          console.error('Update error:', error);
        }
      });
  }

  isEditing(product: Product): boolean {
    return this.editingElement === product;
  }

  initializeDefaults(): void {
    if (!confirm('ACHTUNG: Alle bestehenden Produkte werden gelöscht und durch Standardprodukte ersetzt. Fortfahren?')) {
      return;
    }

    this.setLoading(true);
    this.productService.initializeDefaults()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (products) => {
          this.notification.success(`${products.length} Standardprodukte wurden erstellt`);
          this.loadAll();
        },
        error: (error) => {
          this.notification.error('Fehler beim Initialisieren der Standardprodukte');
          this.handleError(error);
        }
      });
  }
}
