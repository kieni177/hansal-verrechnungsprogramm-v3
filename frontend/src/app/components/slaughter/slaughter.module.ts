import { Routes } from '@angular/router';

/**
 * Slaughter Feature Module
 *
 * This module handles all slaughter-related functionality including:
 * - Slaughter record listing
 * - Slaughter record detail view with meat cuts breakdown
 * - Slaughter record creation and editing
 * - Meat cut tracking
 */

export const SLAUGHTER_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./slaughter.component')
      .then(m => m.SlaughterComponent),
    runGuardsAndResolvers: 'always'
  },
  {
    path: 'create',
    loadComponent: () => import('./slaughter-form/slaughter-form.component')
      .then(m => m.SlaughterFormComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./slaughter-detail/slaughter-detail.component')
      .then(m => m.SlaughterDetailComponent)
  },
  {
    path: 'edit/:id',
    loadComponent: () => import('./slaughter-form/slaughter-form.component')
      .then(m => m.SlaughterFormComponent)
  }
];
