import { Component, output } from '@angular/core';
import { OfferFormComponent } from "./offer-form/offer-form.component";
import { CreateOffer, SkillCategory, SkillOffer, UpdateSkillOffer } from '../../../../../dtos/skill';
import { NgpDialog, NgpDialogOverlay, NgpDialogTrigger } from 'ng-primitives/dialog';
import { SkillService } from '../../../../../services/skill/skill.service';
import { Observable } from 'rxjs';
import { CommonModule } from '@angular/common';
import { UserService } from '../../../../../services/user/user.service';
import { OfferCardComponent } from "./offer-card/offer-card.component";

@Component({
  selector: 'app-offer-step',
  imports: [
    OfferFormComponent,
    NgpDialog,
    NgpDialogOverlay,
    NgpDialogTrigger,
    CommonModule,
    OfferCardComponent
  ],
  templateUrl: './offer-step.component.html',
  styleUrl: './offer-step.component.scss'
})
export class OfferStepComponent {

  // categories: SkillCategory[] = [];
  categories$: Observable<SkillCategory[]>;
  offers: SkillOffer[] = [];
  showOverlay: boolean = true;
  finished = output();
  back = output();

  constructor(private skillService: SkillService, private userService: UserService) {
    this.categories$ = this.skillService.getSkillCategories();
    // this.
    this.fetchOffers();
  }

  fetchOffers() {
    this.userService.getDetailedCurrentUser().subscribe(user => {
      this.offers = user.skillOffers;
    });
  }


  onSubmit(offer: CreateOffer, cb: any) {
    this.skillService.createOffer(offer).subscribe(offer => {
      console.log(offer);
      this.offers.push(offer);
      cb();
      this.fetchOffers();
    });
  }

  onModify(event: (CreateOffer) & { cb: any }, offerId: number, index: number) {
    const offerUpdate = { ...event, id: this.offers[index].id };
    delete offerUpdate["cb"]

    this.skillService.updateOffer(offerUpdate as any as UpdateSkillOffer).subscribe(offer => {
      this.offers[index] = offer;
      event.cb();
      this.fetchOffers();
    });
  }

  onDelete(offerToDelete: SkillOffer, index: number) {
    this.skillService.deleteSkill(offerToDelete.id).subscribe(() => {
      this.offers.splice(index, 1);
      this.fetchOffers();
    });
  }
}
