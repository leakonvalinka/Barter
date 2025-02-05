import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { PasswordRecoveryService } from '../../../../services/password-recovery/password-recovery.service';
import { AuthenticationService } from '../../../../services/auth/auth.service';

@Component({
    selector: 'app-reset-password',
    imports: [CommonModule, ReactiveFormsModule, RouterLink],
    templateUrl: './reset-password.component.html',
    styleUrls: ['./reset-password.component.scss', '../auth.styles.scss']
})
export class ResetPasswordComponent implements OnInit {
  resetPasswordForm: FormGroup;
  jwtToken: string | null = null;
  resetToken: string | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private toastr: ToastrService,
    private router: Router,
    private passwordRecoveryService: PasswordRecoveryService,
    private authService: AuthenticationService
  ) {
    this.resetPasswordForm = this.fb.group({
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.jwtToken = params['jwt'];
      this.resetToken = params['resetToken'];
      
      // Only show error if we have no tokens at all
      if (!this.jwtToken && !this.resetToken) {
        this.toastr.error('Invalid password reset link. Please request a new one.');
        this.router.navigate(['/forgot-password']);
      }
    });
  }

  // Custom validator to check if passwords match
  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    return null;
  }

  onSubmit() {
    if (this.resetPasswordForm.valid && this.resetToken) {
      const { password } = this.resetPasswordForm.value;
      console.log('Resetting password with token:', this.resetToken);
      this.passwordRecoveryService.resetPassword(this.resetToken, password).subscribe({
        next: () => {
          console.log('Password reset successful');
          this.toastr.success('Password reset successful!');
          this.resetPasswordForm.reset();
          this.authService.logout();
          this.router.navigate(['/login']);
        },
        error: (error) => {
          console.error('Password reset failed');
          this.toastr.error('Password reset failed. Please try again.');
        }
      });
    }
  }
}
