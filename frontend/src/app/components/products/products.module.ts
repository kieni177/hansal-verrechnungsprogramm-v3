import { Routes } from '@angular/router';

/**
 * Products Feature Module
 *
 * This module handles all product-related functionality including:
 * - Product listing and search
 * - Product detail view
 * - Product creation and editing
 */

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
