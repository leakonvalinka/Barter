import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PasswordRecoveryService } from '../../../../services/password-recovery/password-recovery.service';
import { ToastrService } from 'ngx-toastr';

@Component({
    selector: 'app-forgot-password',
    imports: [CommonModule, ReactiveFormsModule, RouterLink],
    templateUrl: './forgot-password.component.html',
    styleUrls: ['./forgot-password.component.scss', '../auth.styles.scss']
})
export class ForgotPasswordComponent {
  forgotPasswordForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private passwordRecoveryService: PasswordRecoveryService,
    private toastr: ToastrService
  ) {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit() {
    if (this.forgotPasswordForm.valid) {
      this.passwordRecoveryService.sendResetEmail(this.forgotPasswordForm.value.email).subscribe({
        next: (response) => {
          console.log('If an account with the provided email exists, a reset email will be sent!');
          this.toastr.info('If an account with the provided email exists, a reset email will be sent!');
          this.forgotPasswordForm.reset();
        },
        error: (error) => {
          console.error('Something went wrong during requesting a reset email', error);
          this.toastr.error('Failed to request an reset email. Please try again.');
        }
      });
    }
  }
}
