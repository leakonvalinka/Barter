import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {SkillService} from '../../../../services/skill/skill.service';
import {SkillCategory, UpdateSkillOffer} from '../../../../dtos/skill';
import {NgForOf, NgIf} from '@angular/common';
import {ToastrService} from 'ngx-toastr';
import { ConfirmationDialogService } from '../../../../services/dialog/confirmation-dialog.service';
import { UserService } from '../../../../services/user/user.service';


@Component({
  selector: 'app-skill-edit',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgForOf,
    NgIf
  ],
  templateUrl: './offer-edit.component.html',
  styleUrl: './offer-edit.component.scss'
})
export class OfferEditComponent implements OnInit{
  isAuthorized: boolean | undefined;
  skillId!: number;
  skill: UpdateSkillOffer = {
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
      skillOffers: [],
    },
    schedule: '',
  }
  skillForm!: FormGroup;
  categories: SkillCategory[] = [];
  selectedCategoryId?: number;


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
      schedule: [this.skill?.schedule || '', Validators.required]
    });


    // Get skill ID from route parameters
    this.route.params.subscribe({
      next: (params) => {
        this.skillId = params['id'];
        this.skillService.getSkillOfferById(this.skillId).subscribe({
          next: (skill) => {
            if (!skill) {
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
                  schedule: skill.schedule
                });
                this.skill = skill;
              },
              error: (error) => {
                this.toaster.error('Error authenticating user.');
                this.router.navigate(['/offers']);
              }
            });
          }
        });
      }
    });

    this.skillService.getSkillCategories().subscribe({
      next: categories => {
        this.categories = categories;
      },
      error: err => {
        console.log(err);
      }
    });
  }

  onCategoryChange(event: Event): void {
    const target = event.target as HTMLSelectElement;
    this.selectedCategoryId = Number(target.value);
    this.skill.category.id = this.selectedCategoryId;
  }

  onSubmit(): void {
    if (this.skillForm.valid) {
      this.setFormInputToSkill();
      this.skillService.updateOffer(this.skill).subscribe({
        next: () => {
          this.toaster.success('Offer updated successfully!');
          this.router.navigate(['/offers', this.skillId]);
        },
        error: err => {
          console.log(err);
          this.toaster.error('Could not update offer!');
        }
      });
    }
  }

  async onDelete(): Promise<void> {
    const confirmed = await this.confirmationDialog.confirm({
      title: 'Delete Offer',
      message: 'Are you sure you want to delete this offer? This action cannot be undone.',
      confirmText: 'Delete',
      cancelText: 'Cancel'
    });

    if (confirmed.confirmed) {
      this.skillService.deleteSkill(this.skillId).subscribe({
        next: () => {
          console.log('Skill deleted successfully.');
          this.toaster.success('Offer deleted successfully!');
          this.router.navigate(['/offers']);
        }
      });
    }
  }

  cancelEdit(): void {
    this.router.navigate(['/offers', this.skillId,]);
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
      schedule: this.skillForm.controls['schedule'].value,
    };
  }

  goBack(): void {
    this.router.navigate(['/offers']);
  }
}
