import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Subject } from 'rxjs';
import { takeUntil, switchMap } from 'rxjs/operators';
import { Order } from '../../../models/order.model';
import { Invoice } from '../../../models/invoice.model';
import { OrderService } from '../../../services/order.service';
import { InvoiceService } from '../../../services/invoice.service';
import { NotificationService } from '../../../services/notification.service';
import { ConfirmationDialogComponent, ConfirmationDialogData } from '../../shared/confirmation-dialog/confirmation-dialog.component';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    MatChipsModule,
    MatTableModule,
    MatDialogModule
  ],
  templateUrl: './order-detail.component.html',
  styleUrls: ['./order-detail.component.scss']
})
export class OrderDetailComponent implements OnInit, OnDestroy {
  order?: Order;
  loading: boolean = true;
  orderId?: number;
  displayedColumns = ['product', 'quantity', 'unitPrice', 'subtotal'];
  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private location: Location,
    private orderService: OrderService,
    private invoiceService: InvoiceService,
    private notificationService: NotificationService,
    private dialog: MatDialog
  ) {}

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.orderId = +id;
      this.loadOrder(this.orderId);
    }
  }

  loadOrder(id: number): void {
    this.loading = true;
    this.orderService.getById(id).subscribe({
      next: (order: Order) => {
        this.order = order;
        this.loading = false;
      },
      error: (error: any) => {
        this.notificationService.error('Bestellung konnte nicht geladen werden');
        this.loading = false;
        this.goBack();
      }
    });
  }

  editOrder(): void {
    this.router.navigate(['/orders/edit', this.orderId]);
  }

  createInvoice(): void {
    if (!this.orderId) {
      return;
    }

    // First check if invoice already exists
    this.invoiceService.getByOrderId(this.orderId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (existingInvoice) => {
          if (existingInvoice) {
            // Show confirmation dialog to overwrite
            this.showOverwriteInvoiceDialog(existingInvoice);
            return;
          }

          // Create the invoice if it doesn't exist
          this.doCreateInvoice();
        },
        error: (error) => {
          this.notificationService.error('Fehler beim Prüfen der Rechnung');
          console.error('Check invoice error:', error);
        }
      });
  }

  private showOverwriteInvoiceDialog(existingInvoice: Invoice): void {
    const dialogData: ConfirmationDialogData = {
      title: 'Rechnung überschreiben?',
      message: `Für diese Bestellung existiert bereits eine Rechnung (${existingInvoice.invoiceNumber}). Möchten Sie diese überschreiben und eine neue Rechnung erstellen?`,
      confirmText: 'Überschreiben',
      cancelText: 'Abbrechen'
    };

    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      width: '400px',
      data: dialogData
    });

    dialogRef.afterClosed()
      .pipe(takeUntil(this.destroy$))
      .subscribe(confirmed => {
        if (confirmed) {
          // Delete existing invoice and create new one
          this.invoiceService.delete(existingInvoice.id!)
            .pipe(
              takeUntil(this.destroy$),
              switchMap(() => this.invoiceService.createFromOrder(this.orderId!))
            )
            .subscribe({
              next: (invoice) => {
                this.notificationService.success('Rechnung erfolgreich neu erstellt');
                this.router.navigate(['/invoices']);
              },
              error: (error) => {
                this.notificationService.error('Fehler beim Erstellen der Rechnung');
                console.error('Create invoice error:', error);
              }
            });
        }
      });
  }

  private doCreateInvoice(): void {
    this.invoiceService.createFromOrder(this.orderId!)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (invoice) => {
          this.notificationService.success('Rechnung erfolgreich erstellt');
          this.router.navigate(['/invoices']);
        },
        error: (error) => {
          this.notificationService.error('Fehler beim Erstellen der Rechnung');
          console.error('Create invoice error:', error);
        }
      });
  }

  goBack(): void {
    this.location.back();
  }

  getStatusColor(status: string): string {
    const colors: Record<string, string> = {
      'PENDING': 'accent',
      'CONFIRMED': 'primary',
      'SHIPPED': 'primary',
      'DELIVERED': 'primary',
      'CANCELLED': 'warn'
    };
    return colors[status] || '';
  }
}
