import {Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserInfoComponent } from './user-info/user-info.component';
import { UserUpdate } from '../../../../dtos/user';
import { UserService } from '../../../../services/user/user.service';
import { ToastrService } from 'ngx-toastr';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-profile-edit',
  standalone: true,
  imports: [
    CommonModule,
    UserInfoComponent
  ],
  templateUrl: './profile-edit.component.html',
  styleUrl: './profile-edit.component.scss'
})
export class ProfileEditComponent implements OnInit {
  private username: string = '';
  public buttonConfig = {
    submit: {
      text: 'Save Profile',
      class: 'btn-primary',
      show: true
    },
    cancel: {
      text: 'Go Back',
      class: 'btn-secondary',
      show: true
    }
  };

  constructor(
    private readonly userService: UserService,
    private readonly toaster: ToastrService,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    // get username from route but not with snapshot
    this.route.params.subscribe(params => {
      this.username = params['username'];
    });
  }

  onUserInfoUpdate({ user, dirty }: { user: UserUpdate, dirty: boolean }): void {
    if (!dirty) {
      this.router.navigate(['/profile/', this.username]);
      return;
    }

    this.userService.updateUserInformation(user).subscribe({
      next: () => {
        this.toaster.info('Profile updated successfully!');
        this.router.navigate(['/profile/', this.username]);
      },
      error: (error) => {
        console.error('Profile update error:', error);
        this.toaster.error('Could not update profile!');
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/profile/', this.username]);
  }
}
