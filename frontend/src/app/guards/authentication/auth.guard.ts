import { Injectable } from '@angular/core';
import { ActivatedRoute, ActivatedRouteSnapshot, CanActivate, Router } from '@angular/router';
import { AuthenticationService } from '../../services/auth/auth.service';
import { map, switchMap, Observable, of } from 'rxjs';
import { UserService } from '../../services/user/user.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(
    private authService: AuthenticationService,
    private userService: UserService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean> {
    return this.authService.getAuthState().pipe(
      switchMap(state => {
        const isLoggedIn = state.isAuthenticated;

        const requiredRoles = route.data['roles'] as Array<string>;

        // Special case for PASSWORD-RESET role
        if (!isLoggedIn && requiredRoles.includes('PASSWORD-RESET')) {
          const jwt = route.queryParams['jwt'];
          if (jwt) {
            // Validate and set the token
            const isValid = this.authService.validateAndSetAuthentication(jwt);
            if (isValid) {
              return of(true);
            }
          }
        }

        // Normal authentication check for other roles
        if (!isLoggedIn || (requiredRoles && !requiredRoles.includes(state.role || ''))) {
          const currentUrl = this.router.url;
          this.router.navigate(['/login'], {
            queryParams: { redirectTo: currentUrl }
          });
          return of(false);
        }
        return this.userService.getDetailedCurrentUser()
          .pipe(
            map((user) => {
              if (!user.location || !user.location.postalCode || !user.location.city || !user.location.country || !user.location.street || !user.displayName) {
                this.router.navigate(['/onboarding']);
                return false;
              }
              return true
            })
          );
      })
    );
  }
}
