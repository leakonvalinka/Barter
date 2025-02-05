import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router } from '@angular/router';
import { AuthenticationService } from '../../services/auth/auth.service';
import { Observable, of, from } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { ToastrService } from 'ngx-toastr';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {
  constructor(private authService: AuthenticationService, private router: Router, private toastr: ToastrService) { }

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean> {
    const requiredRoles = route.data['roles'] as string[] | undefined;

    if (!requiredRoles || requiredRoles.length === 0) {
      console.error('No roles specified in route data.');
      return of(false);
    }

    // First check if the token needs refresh
    return this.authService.refreshAccessToken().pipe(
      catchError(() => of(null)), // If refresh fails, continue with current token
      switchMap(() => {
        const hasRole = requiredRoles.some(role => this.authService.hasRole(role));

        if (!hasRole) {
          console.log("You don't have access privileges");
          this.toastr.error("You don't have access privileges");
          this.router.navigate(['/login']);
          return of(false);
        }

        return of(true);
      })
    );
  }
}
