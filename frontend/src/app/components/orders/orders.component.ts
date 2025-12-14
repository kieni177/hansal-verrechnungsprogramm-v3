import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { SelectionModel } from '@angular/cdk/collections';
import { animate, state, style, transition, trigger } from '@angular/animations';
import { takeUntil, switchMap, forkJoin, of } from 'rxjs';

import { BaseCrudComponent } from '../../shared/base/base-crud.component';
import { OrderService } from '../../services/order.service';
import { InvoiceService } from '../../services/invoice.service';
import { NotificationService } from '../../services/notification.service';
import { Order, OrderStatus } from '../../models/order.model';
import { Invoice, InvoiceStatus } from '../../models/invoice.model';
import { API_MESSAGES } from '../../shared/constants/app.constants';
import { ConfirmationDialogComponent, ConfirmationDialogData } from '../shared/confirmation-dialog/confirmation-dialog.component';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatDialogModule
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
  displayedColumns = ['select', 'id', 'customerName', 'orderDate', 'totalAmount', 'status', 'invoice', 'actions'];
  dataSource = new MatTableDataSource<Order>([]);
  searchTerm = '';
  statusFilter = '';
  expandedElement: Order | null = null;
  editingElement: Order | null = null;
  editedOrder: Order | null = null;
  orderStatuses = Object.values(OrderStatus);
  selection = new SelectionModel<Order>(true, []);
  isProcessing = false;
  orderInvoiceMap = new Map<number, Invoice>();

  @ViewChild(MatSort) set sort(sort: MatSort) {
    if (sort) {
      this.dataSource.sort = sort;
    }
  }

  constructor(
    private orderService: OrderService,
    private invoiceService: InvoiceService,
    private notification: NotificationService,
    private router: Router,
    private dialog: MatDialog
  ) {
    super();
  }

  ngOnInit(): void {
    this.loadAll();
    this.setupFilter();
  }

  setupFilter(): void {
    this.dataSource.filterPredicate = (data: Order, filter: string) => {
      const searchStr = filter.toLowerCase();
      const matchesSearch = !this.searchTerm ||
        data.customerName.toLowerCase().includes(searchStr) ||
        (data.id?.toString().includes(searchStr) ?? false);
      const matchesStatus = !this.statusFilter || data.status === this.statusFilter;
      return matchesSearch && matchesStatus;
    };
  }

  applyFilter(): void {
    this.dataSource.filter = this.searchTerm.trim().toLowerCase() + this.statusFilter;
  }

  loadAll(): void {
    this.setLoading(true);

    // Load orders and invoices in parallel
    forkJoin({
      orders: this.orderService.getAll(),
      invoices: this.invoiceService.getAll()
    })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: ({ orders, invoices }) => {
          this.items = orders;
          this.dataSource.data = orders;

          // Build map of order ID -> invoice
          this.orderInvoiceMap.clear();
          invoices.forEach(invoice => {
            if (invoice.order?.id) {
              this.orderInvoiceMap.set(invoice.order.id, invoice);
            }
          });

          this.setLoading(false);
        },
        error: (error) => {
          this.notification.error(API_MESSAGES.ERROR.LOAD);
          this.handleError(error);
        }
      });
  }

  /** Check if order has an invoice */
  hasInvoice(order: Order): boolean {
    return order.id ? this.orderInvoiceMap.has(order.id) : false;
  }

  /** Get invoice for order */
  getInvoice(order: Order): Invoice | undefined {
    return order.id ? this.orderInvoiceMap.get(order.id) : undefined;
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
            // Show confirmation dialog to overwrite
            this.showOverwriteInvoiceDialog(order, existingInvoice);
            return;
          }

          // Create the invoice if it doesn't exist
          this.doCreateInvoice(order.id!);
        },
        error: (error) => {
          this.notification.error('Fehler beim Prüfen der Rechnung');
          console.error('Check invoice error:', error);
        }
      });
  }

  private showOverwriteInvoiceDialog(order: Order, existingInvoice: Invoice): void {
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
              switchMap(() => this.invoiceService.createFromOrder(order.id!))
            )
            .subscribe({
              next: (invoice) => {
                this.notification.success('Rechnung erfolgreich neu erstellt');
                this.router.navigate(['/invoices']);
              },
              error: (error) => {
                this.notification.error('Fehler beim Erstellen der Rechnung');
                console.error('Create invoice error:', error);
              }
            });
        }
      });
  }

  private doCreateInvoice(orderId: number): void {
    this.invoiceService.createFromOrder(orderId)
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

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected(): boolean {
    const numSelected = this.selection.selected.length;
    const numRows = this.items.length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  toggleAllRows(): void {
    if (this.isAllSelected()) {
      this.selection.clear();
      return;
    }
    this.selection.select(...this.items);
  }

  /** Create invoices for all selected orders */
  createInvoicesForSelected(): void {
    const selectedOrders = this.selection.selected;
    if (selectedOrders.length === 0) {
      this.notification.warning('Bitte wählen Sie mindestens eine Bestellung aus');
      return;
    }

    const dialogData: ConfirmationDialogData = {
      title: 'Rechnungen erstellen',
      message: `Möchten Sie für ${selectedOrders.length} Bestellung(en) Rechnungen erstellen? Bestehende Rechnungen werden überschrieben.`,
      confirmText: 'Erstellen',
      cancelText: 'Abbrechen'
    };

    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      width: '450px',
      data: dialogData
    });

    dialogRef.afterClosed()
      .pipe(takeUntil(this.destroy$))
      .subscribe(confirmed => {
        if (confirmed) {
          this.processInvoiceCreation(selectedOrders);
        }
      });
  }

  /** Update/recreate invoices for selected orders (same as create but clearer naming) */
  updateInvoicesForSelected(): void {
    this.createInvoicesForSelected();
  }

  private processInvoiceCreation(orders: Order[]): void {
    this.isProcessing = true;
    let successCount = 0;
    let errorCount = 0;
    let processedCount = 0;

    const processNext = (index: number) => {
      if (index >= orders.length) {
        this.isProcessing = false;
        if (successCount > 0) {
          this.notification.success(`${successCount} Rechnung(en) erfolgreich erstellt`);
        }
        if (errorCount > 0) {
          this.notification.warning(`${errorCount} Rechnung(en) konnten nicht erstellt werden`);
        }
        this.selection.clear();
        this.loadAll();
        return;
      }

      const order = orders[index];
      if (!order.id) {
        processedCount++;
        errorCount++;
        processNext(index + 1);
        return;
      }

      // Check if invoice exists and delete it first
      this.invoiceService.getByOrderId(order.id)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (existingInvoice) => {
            if (existingInvoice) {
              // Delete existing and create new
              this.invoiceService.delete(existingInvoice.id!)
                .pipe(
                  takeUntil(this.destroy$),
                  switchMap(() => this.invoiceService.createFromOrder(order.id!))
                )
                .subscribe({
                  next: () => {
                    successCount++;
                    processNext(index + 1);
                  },
                  error: () => {
                    errorCount++;
                    processNext(index + 1);
                  }
                });
            } else {
              // Create new invoice
              this.invoiceService.createFromOrder(order.id!)
                .pipe(takeUntil(this.destroy$))
                .subscribe({
                  next: () => {
                    successCount++;
                    processNext(index + 1);
                  },
                  error: () => {
                    errorCount++;
                    processNext(index + 1);
                  }
                });
            }
          },
          error: () => {
            errorCount++;
            processNext(index + 1);
          }
        });
    };

    processNext(0);
  }
}
