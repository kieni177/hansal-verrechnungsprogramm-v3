import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { SelectionModel } from '@angular/cdk/collections';
import { animate, state, style, transition, trigger } from '@angular/animations';
import { takeUntil, forkJoin } from 'rxjs';

import { BaseCrudComponent } from '../../shared/base/base-crud.component';
import { InvoiceService } from '../../services/invoice.service';
import { NotificationService } from '../../services/notification.service';
import { Invoice, InvoiceStatus } from '../../models/invoice.model';
import { DownloadUtils } from '../../shared/utils/download.utils';
import { API_MESSAGES } from '../../shared/constants/app.constants';

@Component({
  selector: 'app-invoices',
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
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ],
  templateUrl: './invoices.component.html',
  styleUrls: ['./invoices.component.scss'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({ height: '0px', minHeight: '0' })),
      state('expanded', style({ height: '*' })),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)'))
    ])
  ]
})
export class InvoicesComponent extends BaseCrudComponent<Invoice> implements OnInit {
  displayedColumns = ['select', 'invoiceNumber', 'customerName', 'issueDate', 'grandTotal', 'createdBy', 'status', 'actions'];
  dataSource = new MatTableDataSource<Invoice>([]);
  searchTerm = '';
  statusFilter = '';
  invoiceStatuses = Object.values(InvoiceStatus);
  expandedElement: Invoice | null = null;
  selection = new SelectionModel<Invoice>(true, []);
  isDownloading = false;

  @ViewChild(MatSort) set sort(sort: MatSort) {
    if (sort) {
      this.dataSource.sort = sort;
      // Custom sorting for nested properties
      this.dataSource.sortingDataAccessor = (item: Invoice, property: string) => {
        switch (property) {
          case 'customerName':
            return item.order?.customerName?.toLowerCase() || '';
          default:
            return (item as any)[property];
        }
      };
    }
  }

  constructor(
    private invoiceService: InvoiceService,
    private notification: NotificationService
  ) {
    super();
  }

  ngOnInit(): void {
    this.loadAll();
    this.setupFilter();
  }

  setupFilter(): void {
    this.dataSource.filterPredicate = (data: Invoice, filter: string) => {
      const searchStr = this.searchTerm.toLowerCase();
      const matchesSearch = !this.searchTerm ||
        (data.invoiceNumber?.toLowerCase().includes(searchStr) ?? false) ||
        (data.order?.customerName?.toLowerCase().includes(searchStr) ?? false);
      const matchesStatus = !this.statusFilter || data.status === this.statusFilter;
      return matchesSearch && matchesStatus;
    };
  }

  applyFilter(): void {
    this.dataSource.filter = this.searchTerm.trim().toLowerCase() + this.statusFilter;
  }

  loadAll(): void {
    this.setLoading(true);
    this.invoiceService.getAll()
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

  toggleExpand(invoice: Invoice): void {
    this.expandedElement = this.expandedElement === invoice ? null : invoice;
  }

  downloadPdf(invoice: Invoice): void {
    this.invoiceService.downloadPdf(invoice.id!)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blob) => {
          const customerName = invoice.order?.customerName?.replace(/[^a-zA-ZäöüÄÖÜß\s]/g, '').replace(/\s+/g, '_') || 'kunde';
          DownloadUtils.downloadFile(blob, `beleg_${customerName}.pdf`);
        },
        error: (error) => {
          this.notification.error('PDF konnte nicht heruntergeladen werden');
          console.error('Download error:', error);
        }
      });
  }

  delete(id: number): void {
    if (!confirm('Sind Sie sicher, dass Sie diese Rechnung löschen möchten?')) {
      return;
    }

    this.invoiceService.delete(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notification.success('Rechnung erfolgreich gelöscht');
          this.loadAll();
        },
        error: (error) => {
          this.notification.error('Rechnung konnte nicht gelöscht werden');
          console.error('Delete error:', error);
        }
      });
  }

  getStatusColor(status: string): string {
    const colors: Record<string, string> = {
      'PAID': 'primary',
      'UNPAID': 'accent',
      'OVERDUE': 'warn'
    };
    return colors[status] || '';
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

  /** Download all selected invoices as separate PDFs */
  downloadSelected(): void {
    const selectedInvoices = this.selection.selected;
    if (selectedInvoices.length === 0) {
      this.notification.warning('Bitte wählen Sie mindestens eine Rechnung aus');
      return;
    }

    this.isDownloading = true;

    // Download each PDF sequentially with a small delay to avoid overwhelming the browser
    let downloadIndex = 0;
    const downloadNext = () => {
      if (downloadIndex >= selectedInvoices.length) {
        this.isDownloading = false;
        this.notification.success(`${selectedInvoices.length} PDF(s) erfolgreich heruntergeladen`);
        this.selection.clear();
        return;
      }

      const invoice = selectedInvoices[downloadIndex];
      this.invoiceService.downloadPdf(invoice.id!)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (blob) => {
            const customerName = invoice.order?.customerName?.replace(/[^a-zA-ZäöüÄÖÜß\s]/g, '').replace(/\s+/g, '_') || 'kunde';
            DownloadUtils.downloadFile(blob, `beleg_${customerName}.pdf`);
            downloadIndex++;
            // Small delay between downloads
            setTimeout(downloadNext, 300);
          },
          error: (error) => {
            this.notification.error(`PDF für ${invoice.invoiceNumber} konnte nicht heruntergeladen werden`);
            console.error('Download error:', error);
            downloadIndex++;
            setTimeout(downloadNext, 300);
          }
        });
    };

    downloadNext();
  }

  /** Download all invoices as separate PDFs */
  downloadAll(): void {
    this.selection.select(...this.items);
    this.downloadSelected();
  }

  /** Download selected invoices as ONE combined PDF */
  downloadSelectedCombined(): void {
    const selectedInvoices = this.selection.selected;
    if (selectedInvoices.length === 0) {
      this.notification.warning('Bitte wählen Sie mindestens eine Rechnung aus');
      return;
    }

    this.isDownloading = true;
    const ids = selectedInvoices.map(inv => inv.id!);

    this.invoiceService.downloadCombinedPdf(ids)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blob) => {
          const today = new Date().toISOString().slice(0, 10).replace(/-/g, '');
          DownloadUtils.downloadFile(blob, `belege_sammel_${today}.pdf`);
          this.isDownloading = false;
          this.notification.success(`${selectedInvoices.length} Rechnungen als Sammeldokument heruntergeladen`);
          this.selection.clear();
        },
        error: (error) => {
          this.notification.error('Sammeldokument konnte nicht erstellt werden');
          console.error('Combined PDF error:', error);
          this.isDownloading = false;
        }
      });
  }

  /** Download ALL invoices as ONE combined PDF */
  downloadAllCombined(): void {
    if (this.items.length === 0) {
      this.notification.warning('Keine Rechnungen vorhanden');
      return;
    }

    this.isDownloading = true;
    const ids = this.items.map(inv => inv.id!);

    this.invoiceService.downloadCombinedPdf(ids)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blob) => {
          const today = new Date().toISOString().slice(0, 10).replace(/-/g, '');
          DownloadUtils.downloadFile(blob, `belege_alle_${today}.pdf`);
          this.isDownloading = false;
          this.notification.success(`Alle ${this.items.length} Rechnungen als Sammeldokument heruntergeladen`);
        },
        error: (error) => {
          this.notification.error('Sammeldokument konnte nicht erstellt werden');
          console.error('Combined PDF error:', error);
          this.isDownloading = false;
        }
      });
  }
}
