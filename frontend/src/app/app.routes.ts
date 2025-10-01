import { Routes } from '@angular/router';

/**
 * Application routing configuration
 * Showcases: Route configuration, lazy loading
 */
export const routes: Routes = [
  {
    path: '',
    redirectTo: 'try-it',
    pathMatch: 'full'
  },
  {
    path: 'try-it',
    loadComponent: () => import('./components/try-it/try-it.component')
      .then(m => m.TryItComponent)
  },
  {
    path: 'sessions',
    loadComponent: () => import('./components/sessions-list/sessions-list.component')
      .then(m => m.SessionsListComponent)
  },
  {
    path: 'sessions/:id',
    loadComponent: () => import('./components/traffic-detail/traffic-detail.component')
      .then(m => m.TrafficDetailComponent)
  },
  {
    path: '**',
    redirectTo: 'try-it'
  }
];
