import { AsyncPipe, NgForOf, NgIf } from '@angular/common';
import { Component, model } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { CreateOffer, SkillCategory } from '../../../../dtos/skill';
import { SkillService } from '../../../../services/skill/skill.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-skill-form',
  imports: [
    FormsModule,
    NgForOf,
    NgIf,
    ReactiveFormsModule,
    AsyncPipe
  ],
  templateUrl: './skill-form.component.html',
  styleUrl: './skill-form.component.scss'
})
export class SkillFormComponent {
  offer = model<CreateOffer>({
    title: '',
    description: '',
    category: {
      id: 0,
    },
    schedule: ''
  })
  offerForm: FormGroup;
  categories$: Observable<SkillCategory[]>;

  constructor(private formBuilder: FormBuilder, private skillService: SkillService) {
    this.categories$ = this.skillService.getSkillCategories();
    this.offerForm = this.formBuilder.group({
      title: [this.offer().title, [Validators.required, Validators.maxLength(100), Validators.minLength(5)]],
      description: [this.offer().description, [Validators.required, Validators.maxLength(200), Validators.minLength(10)]],
      category: this.formBuilder.group({
        id: [this.offer().category.id, Validators.required]
      }), // TODO make typeahead
      schedule: [this.offer().schedule, [Validators.required, Validators.maxLength(100), Validators.minLength(5)]]
    })

    this.offerForm.valueChanges.subscribe(value => {
      this.offer.set(value)
    })

    // effect(() => {
    //   this.offerForm.patchValue(this.offer())
    // })
  }

}
