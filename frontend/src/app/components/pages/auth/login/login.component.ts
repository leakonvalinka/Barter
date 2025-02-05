import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { AuthenticationService } from '../../../../services/auth/auth.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, CommonModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss', '../auth.styles.scss']
})
export class LoginComponent {
  loginForm: FormGroup;

  constructor(
    private formBuilder: FormBuilder,
    private toastr: ToastrService,
    private authService: AuthenticationService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });

    this.authService.getAuthState().subscribe(state => {
      if (state.isAuthenticated) {
        this.router.navigate(['/explore']);
      }
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      const email = this.loginForm.get('email')?.value;
      const password = this.loginForm.get('password')?.value;

      this.authService.loginUser({ emailOrUsername: email, password }).subscribe({
        next: (response) => {
          console.log('Login successful', response);
          this.toastr.success('Login successful!');
          this.loginForm.reset();
          if (response.firstLogin) {
            this.router.navigateByUrl("/onboarding")
          }
          this.onLoginSuccess();
        },
        error: (error) => {
          console.error('Login failed', error);
          this.toastr.error('Login failed. ' + error.error.message);
        }
      });
    }
  }

  onLoginSuccess() {
    // Get the redirectTo parameter, default to '/explore' if not present
    this.route.queryParams.subscribe(params => {
      const redirectTo = params['redirectTo'] || '/explore';
      this.router.navigateByUrl(redirectTo);
    });
  }
}
