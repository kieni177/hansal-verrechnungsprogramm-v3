import { Routes } from '@angular/router';

/**
 * Orders Feature Module
 *
 * This module handles all order-related functionality including:
 * - Order listing and filtering
 * - Order detail view
 * - Order creation and editing
 * - Order status management
 */

export const ORDERS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./orders.component')
      .then(m => m.OrdersComponent)
  },
  {
    path: 'create',
    loadComponent: () => import('./order-form/order-form.component')
      .then(m => m.OrderFormComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./order-detail/order-detail.component')
      .then(m => m.OrderDetailComponent)
  },
  {
    path: 'edit/:id',
    loadComponent: () => import('./order-form/order-form.component')
      .then(m => m.OrderFormComponent)
  }
];
