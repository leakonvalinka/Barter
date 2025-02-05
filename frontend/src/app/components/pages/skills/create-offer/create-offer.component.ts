import {Component, EventEmitter, Output} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {NgForOf, NgIf} from "@angular/common";
import {CreateOffer, SkillCategory} from '../../../../dtos/skill';
import {SkillService} from '../../../../services/skill/skill.service';
import {ToastrService} from 'ngx-toastr';
import {Router} from '@angular/router';

@Component({
  selector: 'app-create-offer',
  standalone: true,
    imports: [
        FormsModule,
        NgForOf,
        NgIf,
        ReactiveFormsModule
    ],
  templateUrl: './create-offer.component.html',
  styleUrl: './create-offer.component.scss'
})
export class CreateOfferComponent {
  @Output() closeDialog = new EventEmitter<void>();
  offerForm: FormGroup;
  categories: SkillCategory[] = [];

  constructor(private formBuilder: FormBuilder,
              private skillService: SkillService,
              private toaster: ToastrService,
              private router: Router) {
    this.skillService.getSkillCategories().subscribe({
      next: categories => {
        this.categories = categories;
      },
      error: err => {
        console.log(err);
      }
    })
    this.offerForm = this.formBuilder.group({
      title: ['', [Validators.required, Validators.maxLength(100), Validators.minLength(5)]],
      description: ['', [Validators.required, Validators.maxLength(200), Validators.minLength(10)]],
      category: [0, Validators.required], // TODO make typeahead
      schedule: ['', [Validators.required, Validators.maxLength(100), Validators.minLength(5)]]
    })
  }

  onSubmit() {
    const offer = this.setOfferValues();
    this.skillService.createOffer(offer).subscribe({
      next: offer => {
        this.cancel();
        this.toaster.success('Offer created successfully!');
        this.router.navigate(['/offers', offer.id]);
      },
      error: err => {
        console.log(err);
        this.toaster.error('Could not create offer!');
      }
    });
  }

  cancel() {
    this.closeDialog.emit();
  }

  private setOfferValues(): CreateOffer {
    return {
      title: this.offerForm.controls['title'].value,
      description: this.offerForm.controls['description'].value,
      category: {
        id: this.offerForm.controls['category'].value
      },
      schedule: this.offerForm.controls['schedule'].value
    };
  }
}
