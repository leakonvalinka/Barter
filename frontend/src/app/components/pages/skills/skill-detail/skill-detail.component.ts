import {Component, Input} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from '@angular/router';
import {SkillService} from '../../../../services/skill/skill.service';
import {ToastrService} from 'ngx-toastr';
import {DemandUrgency, Skill, SkillCategory} from '../../../../dtos/skill';
import {CommonModule} from '@angular/common';
import {UserDetail} from '../../../../dtos/user';
import {AuthenticationService} from '../../../../services/auth/auth.service';
import {UserService} from '../../../../services/user/user.service';
import { Location } from '@angular/common';
import { environment } from '../../../../../environments/environment';
import { SkeletonComponent } from '../../../util/skeleton/skeleton.component';
import { signal } from '@angular/core';
import {BarteringService} from '../../../../services/bartering/bartering.service';
import { filter } from 'rxjs/operators';
import { AdminService } from '../../../../services/admin/admin.service';
import { NgpDialog, NgpDialogTitle, NgpDialogDescription, NgpDialogTrigger, NgpDialogOverlay } from 'ng-primitives/dialog';
import { FormsModule } from '@angular/forms';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ConfirmationDialogService } from '../../../../services/dialog/confirmation-dialog.service';

export enum SkillOfferDemandMode {
  offer,
  demand,
}

@Component({
  selector: 'app-skill-detail',
  standalone: true,
  imports: [
    RouterLink,
    CommonModule,
    SkeletonComponent,
    FormsModule
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  templateUrl: './skill-detail.component.html',
  styleUrl: './skill-detail.component.scss'
})
export class SkillDetailComponent {
  mode: SkillOfferDemandMode = SkillOfferDemandMode.offer;
  isOwner = true;
  user: UserDetail | null = null;
  skill: Skill | null = null;
  urgency: DemandUrgency = DemandUrgency.NONE;
  selectedCategory: SkillCategory = {
    id: 0,
    name: '',
    description: '',
  };
  @Input() skillId: number = -1;
  @Input() showEditAndOverviewButtons: boolean = true;
  environment = environment;
  isLoading = signal<boolean>(true);

  headingText: string = '';
  rating: number = 0.0;
  reviewAmount: number = 0;
  demandUrgencies = Object.values(DemandUrgency)
    .filter(value => typeof value === 'number');
  private previousUrl: string | undefined;

  showReportDialog: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private skillService: SkillService,
    private toastr: ToastrService,
    private authService: AuthenticationService,
    private userService: UserService,
    private location: Location,
    private barteringService: BarteringService,
    private adminService: AdminService,
    private confirmationDialog: ConfirmationDialogService
  ) {
    const currentNav = this.router.getCurrentNavigation();
    if (currentNav?.previousNavigation) {
      this.previousUrl = currentNav.previousNavigation.finalUrl?.toString();
    }
  }

  ngOnInit(): void {
    this.isLoading.set(true);

    this.route.data.subscribe(data => {
      this.mode = data['mode'];
    });

    this.route.params.subscribe({
      next: (params) => {
        if (this.skillId == -1) {
          this.skillId = params['id'];
        }
        this.skillService.getSkillById(this.skillId).subscribe({
          next: (skill: Skill) => {
            this.skill = skill;
            this.user = skill.byUser;
            console.log(this.user);

            if (skill.type === 'demand') {
              this.urgency = DemandUrgency[skill.urgency as unknown as keyof typeof DemandUrgency];
            }

            this.isOwner = this.userService.isCurrentUser(this.user?.username);
            this.setHeadingText();
            this.setOwner();
            this.rating = Number(((this.user?.averageRatingHalfStars ?? 0)/2));
            this.reviewAmount = this.user?.numberOfRatings ?? 0;
            this.isLoading.set(false);
          },
          error: (err) => {
            console.error('Error fetching skill:', err);
            //this.toastr.error('Failed to load skill details.');
            this.isLoading.set(false);
          },
        });
      }
    });
  }

  private setHeadingText(): void {
    if (this.isOwner) {
      this.headingText = `${this.mode === SkillOfferDemandMode.offer ? 'Offer' : 'Demand'}`;
    } else {
      this.headingText = `${this.mode === SkillOfferDemandMode.offer ? 'Offer' : 'Demand'} by ${this.user?.displayName}`;
    }
  }

  getUrgencyLabel(level: DemandUrgency): string {
    return level.toString();
  }

  setOwner(): void {
    const userId = this.authService.getCurrentDecodedUserToken()?.sub;
    if (this.user?.username === userId) { //change user to skill if skill has the username of its owner
      this.isOwner = true;
    }
  }

  enableEditing(): void {
    switch (this.mode) {
      case SkillOfferDemandMode.offer:
        this.router.navigate([`/offers/${this.skill?.id}/edit`]);
        break;
      case SkillOfferDemandMode.demand:
        this.router.navigate([`/demands/${this.skill?.id}/edit`]);
    }
  }

  startExchange() {
    this.barteringService.initiateExchange({
      exchanges: [
        {
          skillID: this.skillId
        }
      ],
      chatMessage: {
        content:'Hi, I would like to barter with you! To accept the exchange please reply to this message!'
      }
    }).subscribe(exchangeChat => {
      this.toastr.success('Exchange was initiated successfully');
      this.router.navigate(['/chat', exchangeChat.id]);
    });
  }

  goToOverview(): void {
    if (this.previousUrl?.includes('edit')) {
      window.history.go(-3);
    } else if(this.previousUrl) {
      this.location.back();
    } else {
      this.router.navigate(['']);
    }
  }

  async openReportDialog() {
    const confirmed = await this.confirmationDialog.confirm({
      title: 'Report Skill?',
      message: 'Are you sure you want to report this skill? This action cannot be undone.',
      confirmText: 'Yes, Report',
      cancelText: 'Cancel',
      showTextField: true,
      textFieldLabel: 'Reason for reporting',
      textFieldPlaceholder: 'Please enter the reason for reporting this skill'
    });

    if (confirmed.confirmed) {
      this.adminService.reportSkill(this.skillId, confirmed.text ?? "").subscribe({
        next: () => {
          this.toastr.success('Skill reported successfully');
          this.router.navigate(['/explore']);
        },
        error: (error) => {
          console.error('Error reporting skill:', error);
          this.toastr.error('Error reporting skill');
        }
      });
    }
  }

  protected readonly SkillOfferDemandMode = SkillOfferDemandMode;
  protected readonly DemandUrgency = DemandUrgency;
}
