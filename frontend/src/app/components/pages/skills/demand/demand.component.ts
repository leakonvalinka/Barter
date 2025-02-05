import { Component } from '@angular/core';
import {
  NgpDialog,
  NgpDialogOverlay,
  NgpDialogTrigger
} from 'ng-primitives/dialog';
import {CreateDemandComponent} from '../create-demand/create-demand.component';
import {UserService} from '../../../../services/user/user.service';
import {SkillDemand} from '../../../../dtos/skill';
import {CommonModule} from '@angular/common';
import {Router} from '@angular/router';
import { SkeletonComponent } from '../../../util/skeleton/skeleton.component';
import { signal } from '@angular/core';

@Component({
    selector: 'app-demand',
    standalone: true,
  imports: [
    NgpDialog,
    NgpDialogOverlay,
    NgpDialogTrigger,
    CreateDemandComponent,
    CommonModule,
    SkeletonComponent
  ],
    templateUrl: './demand.component.html',
    styleUrl: './demand.component.scss'
})
export class DemandComponent {
  demands?: SkillDemand[];
  isLoading = signal<boolean>(true);

  constructor(
    private router: Router,
    private userService: UserService,
  ) {
  }

  ngOnInit(): void {
    this.isLoading.set(true);
    this.userService.getDetailedCurrentUser().subscribe({
      next: user => {
        this.demands = user.skillDemands;
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  goToDetails(id: number): void {
    this.router.navigate(['/demands', id]);
  }
}
