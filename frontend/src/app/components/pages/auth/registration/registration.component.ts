import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { UserService } from '../../../../services/user/user.service';
import { User } from '../../../../dtos/user';
import { RouterLink, Router } from '@angular/router';
import { AuthenticationService } from '../../../../services/auth/auth.service';

@Component({
  selector: 'app-registration',
  imports: [ReactiveFormsModule, RouterLink, CommonModule],
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.scss', '../auth.styles.scss']
})
export class RegistrationComponent {
  registrationForm: FormGroup;

  constructor(
    private formBuilder: FormBuilder,
    private toastr: ToastrService,
    private authService: AuthenticationService,
    private router: Router
  ) {
    this.registrationForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_\-+={}[\]:";'<>?,./~|\\])[A-Za-z\d!@#$%^&*()_\-+={}[\]:";'<>?,./~|\\]{8,}$/)]],
      passwordRepeat: ['', Validators.required]
    }, {
      validator: this.passwordMatchValidator
    });
  }

  passwordMatchValidator(group: FormGroup) {
    const password = group?.get('password');
    const passwordRepeat = group?.get('passwordRepeat');
    return password?.value === passwordRepeat?.value ? null : { mismatch: true };
  }

  onSubmit(): void {
    if (this.registrationForm?.valid) {
      const userMail = this.registrationForm?.get('email')?.value as string;
      const user: User = {
        id: '',
        email: userMail,
        password: this.registrationForm?.get('password')?.value,
        username: userMail.substring(0, userMail.indexOf("@")) + "-" + Math.floor(100000 + Math.random() * 900000)
      };

      this.authService.registerUser(user).subscribe({
        next: (response) => {
          console.log('Registration successful', response);
          this.toastr.info('Please verify your account.');
          this.registrationForm?.reset();
          this.router.navigate(['/verify'], { queryParams: { email: user.email } });
        },
        error: (error) => {
          console.error('Registration failed', error);
          this.toastr.error('Registration failed. Please try again.');
        }
      });
    }
  }
}
