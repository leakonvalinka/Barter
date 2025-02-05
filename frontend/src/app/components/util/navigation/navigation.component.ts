import { Component } from '@angular/core';
import { RouterLink, RouterModule } from '@angular/router';
import { AuthenticationService } from '../../../services/auth/auth.service';
import { CommonModule } from '@angular/common';
@Component({
    selector: 'app-navigation',
    imports: [RouterModule, RouterLink, CommonModule],
    templateUrl: './navigation.component.html',
    styleUrl: './navigation.component.scss'
})
export class NavigationComponent {

  userRole: string | null = null;

  constructor(private authService: AuthenticationService) { }

  ngOnInit() {
    this.authService.getAuthState().subscribe((authState) => {
      this.userRole = authState.role;
    });
  }

  getCurrentUsername() {
    return this.authService.getCurrentDecodedUserToken()?.sub;
  }

}
