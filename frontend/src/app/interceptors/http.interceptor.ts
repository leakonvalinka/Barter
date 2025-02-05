import { HttpContext, HttpContextToken, HttpErrorResponse, HttpEvent, HttpHandlerFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Observable, throwError, BehaviorSubject, of } from 'rxjs';
import { catchError, switchMap, filter, take } from 'rxjs/operators';
import { AuthenticationService } from '../services/auth/auth.service';
import { environment } from '../../environments/environment';

export const BYPASS_AUTH = new HttpContextToken(() => false);

// Track refresh token state
let isRefreshing = false;
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

export function httpInterceptor(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> {
  const router = inject(Router);
  const toast = inject(ToastrService);
  const authService = inject(AuthenticationService);

  // Skip token for login and refresh endpoints
  if (req.context.get(BYPASS_AUTH)) {
    return next(req);
  }

  const modifiedRequest = addAuthHeader(req);

  return next(modifiedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        return handle401Error(error, req, next, router, toast, authService);
      }

      if (error.status === 403) {
        console.log('Forbidden access');
        toast.error('Access forbidden');
      }

      return throwError(() => error);
    })
  );
}

function addAuthHeader(req: HttpRequest<unknown>): HttpRequest<unknown> {
  const token = localStorage.getItem('authToken');
  return req.clone({
    setHeaders: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    }
  });
}

function handle401Error(
  error: HttpErrorResponse,
  request: HttpRequest<unknown>,
  next: HttpHandlerFn,
  router: Router,
  toast: ToastrService,
  authService: AuthenticationService
): Observable<HttpEvent<unknown>> {
  // If we're not already refreshing
  if (!isRefreshing) {
    isRefreshing = true;
    refreshTokenSubject.next(null);

    return authService.refreshAccessToken().pipe(
      switchMap(response => {
        isRefreshing = false;
        refreshTokenSubject.next(response.jwt);

        // Retry the failed request with new token
        return next(addAuthHeader(request));
      }),
      catchError(refreshError => {
        isRefreshing = false;
        authService.logout();
        const currentUrl = router.url;
        toast.error('Session expired - please login again');
        router.navigate(['/login'], {
          queryParams: { redirectTo: currentUrl }
        });
        return throwError(() => refreshError);
      })
    );
  }

  // If we're already refreshing, wait for the new token
  return refreshTokenSubject.pipe(
    filter(token => token !== null),
    take(1),
    switchMap(() => next(addAuthHeader(request)))
  );
}
