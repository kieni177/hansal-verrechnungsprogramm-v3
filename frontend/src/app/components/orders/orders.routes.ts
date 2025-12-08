import { Routes } from '@angular/router';

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
    path: 'edit/:id',
    loadComponent: () => import('./order-form/order-form.component')
      .then(m => m.OrderFormComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./order-detail/order-detail.component')
      .then(m => m.OrderDetailComponent)
  }
];
