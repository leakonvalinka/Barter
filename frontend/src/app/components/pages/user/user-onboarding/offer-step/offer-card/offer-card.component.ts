import { Component, input, output } from '@angular/core';
import { CreateOffer, SkillCategory, SkillOffer, UpdateSkillOffer } from '../../../../../../dtos/skill';
import { OfferFormComponent } from "../offer-form/offer-form.component";
import { NgpDialog, NgpDialogOverlay, NgpDialogTrigger } from 'ng-primitives/dialog';

@Component({
  selector: 'app-offer-card',
  imports: [
    OfferFormComponent,
    NgpDialog,
    NgpDialogOverlay,
    NgpDialogTrigger,
  ],
  templateUrl: './offer-card.component.html',
  styleUrl: './offer-card.component.scss'
})
export class OfferCardComponent {


  offer = input.required<SkillOffer>();
  categories = input.required<SkillCategory[]>();
  modify = output<(UpdateSkillOffer | CreateOffer) & { cb: any }>();
  delete = output<number>();

  onDelete() {
    throw new Error('Method not implemented.');
  }

  onSubmit($event: CreateOffer | UpdateSkillOffer, cb: any) {
    this.modify.emit({ ...$event, cb })
  }

}
