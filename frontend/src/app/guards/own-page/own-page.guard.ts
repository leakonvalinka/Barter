import { CanActivateFn, Router } from '@angular/router';
import { AuthenticationService } from '../../services/auth/auth.service';
import { inject } from '@angular/core';

export const ownPageGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthenticationService);
  const router = inject(Router);

  const userId = authService.getCurrentDecodedUserToken()?.sub;
  console.log("userId", userId);
  const routeUserId = route.params['username'];
  console.log("routeUserId", routeUserId);

  if (userId === routeUserId) {
    return true;
  } else {
    // Redirect to an unauthorized page or login
    return router.createUrlTree(['/profile', userId]);
  }

};
