import { AsyncPipe, CommonModule } from '@angular/common';
import { Component, effect, signal, WritableSignal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink, RouterModule } from '@angular/router';
import { CreateOffer } from '../../../../dtos/skill';
import { SkillFormComponent } from '../skill-form/skill-form.component';
import { createWatch } from '@angular/core/primitives/signals';
import { OfferStepComponent } from "./offer-step/offer-step.component";
import { UserInfoButtons, UserInfoComponent } from "../profile-edit/user-info/user-info.component";
import { ToastrService } from 'ngx-toastr';
import { UserService } from '../../../../services/user/user.service';
import { UserUpdate } from '../../../../dtos/user';

@Component({
  selector: 'app-user-onboarding',
  imports: [
    ReactiveFormsModule,
    CommonModule,
    OfferStepComponent,
    UserInfoComponent,
    RouterModule,
  ],
  templateUrl: './user-onboarding.component.html',
  styleUrl: './user-onboarding.component.scss'
})
export class UserOnboardingComponent {
  showOverlay: boolean = true;

  step = signal<"profile" | "offers">("profile")

  constructor(private userService: UserService, private toaster: ToastrService, private router: Router, private route: ActivatedRoute) {
    effect(() => {
      this.router.navigate([], {
        queryParams: { step: this.step() },
        queryParamsHandling: 'merge', // merge with existing query params
      });
    })

    this.route.queryParams.subscribe(params => {
      const step = params["step"];
      if (step && step !== this.step() && (step === "offers" || step === "profile")) {
        this.step.set(step);
      }
    });
  }

  buttonConfig: UserInfoButtons = {
    submit: {
      text: 'Next',
      class: 'btn-primary',
      show: true
    },
    cancel: {
      show: false
    }
  };

  onUserInfoUpdate({ user, dirty }: { user: UserUpdate, dirty: boolean }): void {
    if (!dirty) {
      this.step.set("offers");
      return;
    };
    this.userService.updateUserInformation(user).subscribe({
      next: () => {
        this.toaster.info('Profile updated successfully!');
        this.step.set("offers");
      },
      error: (error) => {
        console.error('Profile update error:', error);
        this.toaster.error('Could not update profile!');
      }
    });
  }

  finish() {
    this.router.navigate(['/explore']);
  }
}
