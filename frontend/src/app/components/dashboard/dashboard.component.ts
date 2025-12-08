import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Subject, takeUntil, forkJoin } from 'rxjs';

import { ProductService } from '../../services/product.service';
import { OrderService } from '../../services/order.service';
import { InvoiceService } from '../../services/invoice.service';
import { AdminService } from '../../services/admin.service';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatGridListModule,
    MatIconModule,
    MatButtonModule,
    MatSnackBarModule,
    MatDialogModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  isResetting = false;

  counts = {
    products: 0,
    orders: 0,
    invoices: 0
  };

  constructor(
    private productService: ProductService,
    private orderService: OrderService,
    private invoiceService: InvoiceService,
    private adminService: AdminService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadCounts();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadCounts(): void {
    forkJoin({
      products: this.productService.getAll(),
      orders: this.orderService.getAll(),
      invoices: this.invoiceService.getAll()
    })
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (data) => {
        this.counts = {
          products: data.products.length,
          orders: data.orders.length,
          invoices: data.invoices.length
        };
      },
      error: (error) => console.error('Failed to load dashboard data:', error)
    });
  }

  resetDatabase(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Datenbank zurücksetzen?',
        message: 'WARNUNG: Dies wird ALLE Daten (Bestellungen, Rechnungen, Produkte) löschen und die Standardprodukte neu laden. Diese Aktion kann nicht rückgängig gemacht werden!',
        confirmText: 'Zurücksetzen',
        cancelText: 'Abbrechen',
        isDangerous: true
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.performReset();
      }
    });
  }

  private performReset(): void {
    this.isResetting = true;
    this.adminService.resetDatabase()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.isResetting = false;
          if (response.success) {
            this.snackBar.open(
              `Datenbank erfolgreich zurückgesetzt! ${response.productsLoaded} Produkte geladen.`,
              'Schließen',
              { duration: 5000 }
            );
            this.loadCounts();
          }
        },
        error: (error) => {
          this.isResetting = false;
          this.snackBar.open(
            'Fehler beim Zurücksetzen der Datenbank: ' + error.message,
            'Schließen',
            { duration: 5000 }
          );
        }
      });
  }
}
