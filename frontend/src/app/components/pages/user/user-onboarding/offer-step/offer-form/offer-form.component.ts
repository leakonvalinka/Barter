import { Component, effect, EventEmitter, input, output, Output } from '@angular/core';
import { CreateOffer, SkillCategory, UpdateSkillOffer } from '../../../../../../dtos/skill';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-offer-form',
  imports: [
    FormsModule,
    CommonModule,
    ReactiveFormsModule
  ],

  templateUrl: './offer-form.component.html',
  styleUrl: './offer-form.component.scss'
})
export class OfferFormComponent {

  readonly categories = input.required<SkillCategory[]>();

  readonly offer = input<CreateOffer>();

  cancel = output<void>();
  submit = output<CreateOffer | UpdateSkillOffer>();

  offerForm: FormGroup<{
    title: FormControl<string>,
    description: FormControl<string>,
    category: FormControl<number>,
    schedule: FormControl<string>

  }>;

  constructor(private fb: FormBuilder) {

    this.offerForm = this.fb.nonNullable.group({
      title: [this.offer()?.title ?? '', [Validators.required, Validators.maxLength(100), Validators.minLength(5)]],
      description: [this.offer()?.description ?? '', [Validators.required, Validators.maxLength(200), Validators.minLength(10)]],
      category: [this.offer()?.category.id ?? 0, Validators.required], // TODO make typeahead
      schedule: [this.offer()?.schedule ?? '', [Validators.required, Validators.maxLength(100), Validators.minLength(5)]]
    })

    effect(() => {
      const offer = this.offer();
      this.offerForm.patchValue({
        title: offer?.title ?? '',
        description: offer?.description ?? '',
        category: offer?.category.id ?? 0,
        schedule: offer?.schedule ?? ''
      });
    })
  }

  onCancel() {
    this.offerForm.patchValue({
      title: this.offer()?.title ?? '',
      description: this.offer()?.description ?? '',
      category: this.offer()?.category.id ?? 0,
      schedule: this.offer()?.schedule ?? ''
    });
    this.cancel.emit();
  }

  onSubmit(event: Event) {
    event.stopPropagation();
    this.submit.emit({
      title: this.offerForm.controls['title'].value,
      description: this.offerForm.controls['description'].value,
      category: {
        id: this.offerForm.controls['category'].value
      },
      schedule: this.offerForm.controls['schedule'].value
    });
  }
}
