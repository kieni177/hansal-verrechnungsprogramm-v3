import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { animate, state, style, transition, trigger } from '@angular/animations';
import { takeUntil } from 'rxjs';

import { BaseCrudComponent } from '../../shared/base/base-crud.component';
import { OrderService } from '../../services/order.service';
import { InvoiceService } from '../../services/invoice.service';
import { NotificationService } from '../../services/notification.service';
import { Order, OrderStatus } from '../../models/order.model';
import { API_MESSAGES } from '../../shared/constants/app.constants';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ],
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.scss'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({ height: '0px', minHeight: '0' })),
      state('expanded', style({ height: '*' })),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)'))
    ])
  ]
})
export class OrdersComponent extends BaseCrudComponent<Order> implements OnInit {
  displayedColumns = ['id', 'customerName', 'orderDate', 'totalAmount', 'status', 'actions'];
  expandedElement: Order | null = null;
  editingElement: Order | null = null;
  editedOrder: Order | null = null;
  orderStatuses = Object.values(OrderStatus);

  constructor(
    private orderService: OrderService,
    private invoiceService: InvoiceService,
    private notification: NotificationService,
    private router: Router
  ) {
    super();
  }

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.setLoading(true);
    this.orderService.getAll()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (items) => {
          this.items = items;
          this.setLoading(false);
        },
        error: (error) => {
          this.notification.error(API_MESSAGES.ERROR.LOAD);
          this.handleError(error);
        }
      });
  }

  add(): void {
    this.router.navigate(['/orders/create']);
  }

  viewDetails(order: Order): void {
    this.router.navigate(['/orders', order.id]);
  }

  edit(order: Order): void {
    this.router.navigate(['/orders/edit', order.id]);
  }

  delete(id: number): void {
    this.orderService.delete(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notification.success(API_MESSAGES.SUCCESS.DELETE);
          this.loadAll();
        },
        error: (error) => {
          this.notification.error(API_MESSAGES.ERROR.DELETE);
          console.error('Delete error:', error);
        }
      });
  }

  createInvoice(order: Order): void {
    if (!order.id) {
      return;
    }

    // First check if invoice already exists
    this.invoiceService.getByOrderId(order.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (existingInvoice) => {
          if (existingInvoice) {
            this.notification.error('Für diese Bestellung existiert bereits eine Rechnung');
            return;
          }

          // Create the invoice if it doesn't exist
          this.invoiceService.createFromOrder(order.id!)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: (invoice) => {
                this.notification.success('Rechnung erfolgreich erstellt');
                this.router.navigate(['/invoices']);
              },
              error: (error) => {
                this.notification.error('Fehler beim Erstellen der Rechnung');
                console.error('Create invoice error:', error);
              }
            });
        },
        error: (error) => {
          this.notification.error('Fehler beim Prüfen der Rechnung');
          console.error('Check invoice error:', error);
        }
      });
  }

  getStatusColor(status: string): string {
    const colors: Record<string, string> = {
      'PENDING': 'accent',
      'PROCESSING': 'primary',
      'COMPLETED': 'primary',
      'CANCELLED': 'warn'
    };
    return colors[status] || '';
  }

  toggleExpand(order: Order): void {
    if (this.editingElement === order) {
      return; // Don't collapse while editing
    }
    this.expandedElement = this.expandedElement === order ? null : order;
  }

  startEdit(order: Order): void {
    this.editingElement = order;
    this.editedOrder = { ...order };
    this.expandedElement = order; // Ensure row is expanded
  }

  cancelEdit(): void {
    this.editingElement = null;
    this.editedOrder = null;
  }

  saveEdit(): void {
    if (!this.editedOrder || !this.editedOrder.id) {
      return;
    }

    this.orderService.update(this.editedOrder.id, this.editedOrder)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notification.success(API_MESSAGES.SUCCESS.UPDATE);
          this.editingElement = null;
          this.editedOrder = null;
          this.loadAll();
        },
        error: (error) => {
          this.notification.error(API_MESSAGES.ERROR.UPDATE);
          console.error('Update error:', error);
        }
      });
  }

  isEditing(order: Order): boolean {
    return this.editingElement === order;
  }
}
