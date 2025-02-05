import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { UserService } from '../../../../services/user/user.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { VerificationRequest } from '../../../../dtos/user';
import { HttpErrorResponse } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { AuthenticationService } from '../../../../services/auth/auth.service';

@Component({
  selector: 'app-verify',
  imports: [ReactiveFormsModule, RouterLink, CommonModule],
  templateUrl: './verify.component.html',
  styleUrls: ['./verify.component.scss', '../auth.styles.scss']
})
export class VerifyComponent {
  verificationForm: FormGroup;

  constructor(
    private formBuilder: FormBuilder,
    private toastr: ToastrService,
    private authService: AuthenticationService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.verificationForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      verificationCode: ['', [Validators.required, Validators.maxLength(6), Validators.minLength(6), Validators.pattern(/\d{6}/)]]
    });
  }

  ngOnInit(): void {
    const queryParams = this.route.snapshot.queryParams;
    let email = "";
    if (queryParams["email"]) {
      email = queryParams["email"];
    }
    let verificationCode = "";
    if (queryParams["verificationCode"]) {
      verificationCode = queryParams["verificationCode"];
    }
    this.verificationForm.patchValue({
      email,
      verificationCode
    })

    if (this.verificationForm.valid) {
      this.onSubmit();
    }
  }

  onSubmit(): void {
    if (this.verificationForm?.valid) {
      const verificationRequest: VerificationRequest = {
        email: this.verificationForm?.get('email')?.value,
        verificationToken: this.verificationForm?.get('verificationCode')?.value,

      };

      this.authService.verifyUser(verificationRequest).subscribe({
        next: (response) => {
          console.log('Verification successful', response);
          this.toastr.success('Verification successful!');
          this.verificationForm?.reset();
          this.router.navigate(['/onboarding']);
        },
        error: (error: HttpErrorResponse) => {
          console.error('Registration failed', error);
          if (error.status == 400) {
            this.toastr.error('Registration failed. You have been sent a new verification code.');
            this.verificationForm.patchValue({
              verificationCode: ""
            })
          }
          else if (error.status == 409) {
            this.toastr.warning('Your account has already been verified, please login to continue');
            this.router.navigate(['/login']);
          }
          else {
            this.toastr.success('Your account was successfully verified!');
            this.router.navigate(['/']);
          }

        }
      });
    }
  }
}
