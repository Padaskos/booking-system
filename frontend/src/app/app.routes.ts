import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login')
        .then(component => component.Login)
  },
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  }
];
