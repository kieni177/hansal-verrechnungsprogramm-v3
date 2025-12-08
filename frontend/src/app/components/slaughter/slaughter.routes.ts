import { Routes } from '@angular/router';

export const SLAUGHTER_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./slaughter.component')
      .then(m => m.SlaughterComponent)
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
