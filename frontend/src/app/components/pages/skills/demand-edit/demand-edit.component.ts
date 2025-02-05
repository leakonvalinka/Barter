import {Component, OnInit} from '@angular/core';
import {SkillService} from '../../../../services/skill/skill.service';
import {DemandUrgency, SkillCategory, UpdateSkillDemand} from '../../../../dtos/skill';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ToastrService} from 'ngx-toastr';
import {ConfirmationDialogService} from '../../../../services/dialog/confirmation-dialog.service';
import { UserService } from '../../../../services/user/user.service';


@Component({
  selector: 'app-demand-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './demand-edit.component.html',
  styleUrl: './demand-edit.component.scss'
})
export class DemandEditComponent implements OnInit {
  protected readonly DemandUrgency = DemandUrgency;
  isAuthorized: boolean | undefined;
  skillId!: number;
  skill: UpdateSkillDemand = {
    id: 0,
    title: '',
    description: '',
    category: {
      id: 0,
    },
    byUser: {
      id: 0,
      email: '',
      username: '',
      displayName: '',
      bio: '',
      profilePicture: '',
      location: {
        street: '',
        streetNumber: '',
        city: '',
        postalCode: 0,
        country: '',
        homeLocation: {
          type: '',
          coordinates: []
        }
      },
      skillDemands: [],
      skillOffers: []
    },
    urgency: this.DemandUrgency.NONE,
  }
  skillForm!: FormGroup;
  categories: SkillCategory[] = [];
  selectedCategory: SkillCategory = {
    id: 0,
    name: '',
    description: '',
  };
  selectedCategoryId?: number;
  demandUrgencies = Object.values(DemandUrgency)
    .filter(value => typeof value === 'number'); // Extract numeric values only
  demandUrgencyOptions = [
    { label: 'None', value: DemandUrgency.NONE, isClicked: true },
    { label: 'Low', value: DemandUrgency.LOW, isClicked: false  },
    { label: 'Medium', value: DemandUrgency.MEDIUM, isClicked: false  },
    { label: 'High', value: DemandUrgency.HIGH, isClicked: false  },
    { label: 'Critical', value: DemandUrgency.CRITICAL, isClicked: false }
  ];


  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private skillService: SkillService,
    private toaster: ToastrService,
    private confirmationDialog: ConfirmationDialogService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    // Initialize the form
    this.skillForm = this.fb.group({
      title: [this.skill?.title || '', Validators.required],
      description: [this.skill?.description || '', Validators.required],
      category: [this.skill?.category?.id || '', Validators.required],
      urgency: [this.skill?.urgency || 'NONE', Validators.required]
    });

    // Get skill ID from route parameters
    this.route.params.subscribe({
      next: (params) => {
        this.skillId = params['id'];
        this.skillService.getSkillDemandById(this.skillId).subscribe({
          next: (skill) => {
            if (!skill) {
              console.error('Skill not found for ID:', this.skillId);
              // Handle the case where skill is null
              this.toaster.error('Skill with this ID does not exist.');
              return;
            }

            this.userService.getDetailedCurrentUser().subscribe({
              next: (currentUser) => {
                if (currentUser.username !== skill.byUser.username) {
                  this.isAuthorized = false;
                  this.toaster.error('You are not authorized to edit this offer.');
                  return;
                }

                this.isAuthorized = true;
                this.skillForm.patchValue({
                  title: skill.title,
                  description: skill.description,
                  category: skill.category.id,
                  urgency: skill.urgency
                });
                this.skill = skill;
                this.setActiveUrgencyButton(skill.urgency);
              },
              error: (error) => {
                this.toaster.error('Error authenticating user.');
                this.router.navigate(['/demands']);
              }
            });
          }
       });
      }
    })


    this.skillService.getSkillCategories().subscribe({
      next: categories => {
        this.categories = categories;
      },
      error: err => {
        console.log(err);
      }
    })
  }

  onCategoryChange(event: Event): void {
    const target = event.target as HTMLSelectElement;
    this.selectedCategoryId = Number(target.value);
    this.skill.category.id = this.selectedCategoryId;
  }

  onSubmit(): void {
    if (this.skillForm.valid) {
      this.setFormInputToSkill();
      this.skillService.updateDemand(this.skill).subscribe({
        next: () => {
          this.toaster.success('Demand updated successfully!');
          this.router.navigate(['/demands', this.skillId]);
        },
        error: err => {
          console.log(err);
          this.toaster.error('Could not update demand!');
        }
      });
    }
  }

  async onDelete(): Promise<void> {
    const confirmed = await this.confirmationDialog.confirm({
      title: 'Delete Demand',
      message: 'Are you sure you want to delete this demand? This action cannot be undone.',
      confirmText: 'Delete',
      cancelText: 'Cancel'
    });

    if (confirmed.confirmed) {
      this.skillService.deleteSkill(this.skillId).subscribe({
        next: () => {
          this.toaster.success('Demand deleted successfully!');
          this.router.navigate(['/demands']);
        }
      });
    }
  }

  cancelEdit(): void {
    this.router.navigate(['/demands', this.skillId]);
  }

  setUrgency(level: DemandUrgency): void {
    this.skillForm.patchValue({ urgency: level });
    this.skill.urgency = level;
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

  getActiveButton(level: DemandUrgency): boolean {
    for (const option of this.demandUrgencyOptions) {
      if (option.value === level && option.isClicked) return true;
    }
    return false;
  }

  private setFormInputToSkill() {
    this.skill = {
      id: this.skill.id,
      title: this.skillForm.controls['title'].value,
      description: this.skillForm.controls['description'].value,
      category: {
        id: this.skillForm.controls['category'].value
      },
      byUser: this.skill.byUser,
      urgency: this.skillForm.controls['urgency'].value
    };
  }

  goBack(): void {
    this.router.navigate(['/demands']);
  }
}
