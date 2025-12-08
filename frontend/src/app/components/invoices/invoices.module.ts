import { Routes } from '@angular/router';

/**
 * Invoices Feature Module
 *
 * This module handles all invoice-related functionality including:
 * - Invoice listing
 * - Invoice PDF generation and download
 * - Invoice status tracking
 */

export const INVOICES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./invoices.component')
      .then(m => m.InvoicesComponent)
  }
];
