import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  {
    path: 'dashboard',
    loadComponent: () => import('./components/dashboard/dashboard.component')
      .then(m => m.DashboardComponent)
  },
  {
    path: 'products',
    loadChildren: () => import('./components/products/products.module')
      .then(m => m.PRODUCTS_ROUTES)
  },
  {
    path: 'orders',
    loadChildren: () => import('./components/orders/orders.module')
      .then(m => m.ORDERS_ROUTES)
  },
  {
    path: 'invoices',
    loadChildren: () => import('./components/invoices/invoices.module')
      .then(m => m.INVOICES_ROUTES)
  },
  {
    path: 'slaughter',
    loadChildren: () => import('./components/slaughter/slaughter.module')
      .then(m => m.SLAUGHTER_ROUTES)
  },
  {
    path: 'login',
    loadComponent: () => import('./components/login/login.component')
      .then(m => m.LoginComponent)
  },
  { path: '**', redirectTo: '/dashboard' }
];
