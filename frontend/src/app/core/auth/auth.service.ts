import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { LoginRequest, UserDetails } from './auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly currentUserSubject = new BehaviorSubject<UserDetails | null>(null);
  readonly currentUser$ = this.currentUserSubject.asObservable();

  login(request: LoginRequest): Observable<UserDetails> {
    return this.http.post<UserDetails>('/api/auth/login', request).pipe(
      tap(user => this.currentUserSubject.next(user))
    );
  }
}
