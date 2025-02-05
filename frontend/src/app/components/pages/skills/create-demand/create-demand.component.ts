import {Component, EventEmitter, Output} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {NgForOf, NgIf} from '@angular/common';
import {CreateDemand, DemandUrgency, SkillCategory} from '../../../../dtos/skill';
import {Router} from '@angular/router';
import {SkillService} from '../../../../services/skill/skill.service';
import {ToastrService} from 'ngx-toastr';

@Component({
    selector: 'app-create-demand',
    standalone: true,
    imports: [
        ReactiveFormsModule,
        NgIf,
        NgForOf,
    ],
    templateUrl: './create-demand.component.html',
    styleUrl: './create-demand.component.scss'
})
export class CreateDemandComponent {
  @Output() closeDialog = new EventEmitter<void>();
  demandForm: FormGroup;
  categories: SkillCategory[] = [];
  demandUrgencies = Object.values(DemandUrgency)
    .filter(value => typeof value === 'number'); // Extract numeric values only
  demandUrgencyOptions = [
    { label: 'None', value: DemandUrgency.NONE, isClicked: true },
    { label: 'Low', value: DemandUrgency.LOW, isClicked: false  },
    { label: 'Medium', value: DemandUrgency.MEDIUM, isClicked: false  },
    { label: 'High', value: DemandUrgency.HIGH, isClicked: false  },
    { label: 'Critical', value: DemandUrgency.CRITICAL, isClicked: false }
  ];

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
    });
    this.demandForm = this.formBuilder.group({
      title: ['', [Validators.required, Validators.maxLength(100), Validators.minLength(5)]],
      description: ['', [Validators.required, Validators.maxLength(200), Validators.minLength(10)]],
      category: [0, Validators.required], // TODO make typeahead
      urgency: [this.demandUrgencyOptions[0], Validators.required]
    });
  }

  onSubmit() {
    const demand = this.setDemandValues();
    this.skillService.createDemand(demand).subscribe({
      next: demand => {
        this.cancel();
        this.toaster.success('Demand created successfully!');
        this.router.navigate(['/demands', demand.id]);
      },
      error: err => {
        console.log(err);
        this.toaster.error('Could not create demand!');
      }
    });
  }

  cancel() {
    this.closeDialog.emit();
  }

  private setDemandValues(): CreateDemand {
    return {
      title: this.demandForm.controls['title'].value,
      description: this.demandForm.controls['description'].value,
      category: {
        id: this.demandForm.controls['category'].value
      },
      urgency: this.demandForm.controls['urgency'].value
    };
  }

  setUrgency(level: DemandUrgency): void {
    this.demandForm.patchValue({ urgency: level });
    this.setActiveUrgencyButton(level);
  }

  setActiveUrgencyButton(urgency: DemandUrgency): void {
    for (const option of this.demandUrgencyOptions) {
      option.isClicked = urgency === option.value;
    }
  }

  getUrgencyLabel(level: DemandUrgency): string {
    return DemandUrgency[level];
  }

  protected readonly DemandUrgency = DemandUrgency;
}
