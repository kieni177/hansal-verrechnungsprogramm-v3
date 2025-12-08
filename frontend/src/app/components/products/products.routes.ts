import { Routes } from '@angular/router';

export const PRODUCTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./products.component')
      .then(m => m.ProductsComponent)
  },
  {
    path: 'create',
    loadComponent: () => import('./product-form/product-form.component')
      .then(m => m.ProductFormComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./product-detail/product-detail.component')
      .then(m => m.ProductDetailComponent)
  },
  {
    path: 'edit/:id',
    loadComponent: () => import('./product-form/product-form.component')
      .then(m => m.ProductFormComponent)
  }
];
