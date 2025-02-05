import { Component } from '@angular/core';
import {
  NgpDialog,
  NgpDialogOverlay,
  NgpDialogTrigger
} from "ng-primitives/dialog";
import {CreateOfferComponent} from '../create-offer/create-offer.component';
import {CommonModule} from '@angular/common';
import {SkillOffer} from '../../../../dtos/skill';
import {SkillService} from '../../../../services/skill/skill.service';
import {ToastrService} from 'ngx-toastr';
import {Router} from '@angular/router';
import {UserService} from '../../../../services/user/user.service';
import { SkeletonComponent } from '../../../util/skeleton/skeleton.component';
import { signal } from '@angular/core';

@Component({
  selector: 'app-offer',
  standalone: true,
  imports: [
    NgpDialog,
    NgpDialogOverlay,
    NgpDialogTrigger,
    CreateOfferComponent,
    CommonModule,
    SkeletonComponent
  ],
  templateUrl: './offer.component.html',
  styleUrl: './offer.component.scss'
})
export class OfferComponent {
  offers?: SkillOffer[];
  isLoading = signal<boolean>(true);

  constructor(
    private skillService: SkillService,
    private toastr: ToastrService,
    private router: Router,
    private userService: UserService,
  ) {
  }

  ngOnInit(): void {
    this.isLoading.set(true);
    this.userService.getDetailedCurrentUser().subscribe({
      next: user => {
        this.offers = user.skillOffers;
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  goToDetails(id: number): void {
    this.router.navigate(['/offers', id]);
  }
}
