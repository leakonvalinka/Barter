import {Component, OnInit, CUSTOM_ELEMENTS_SCHEMA, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UserService} from '../../../services/user/user.service';
import {SkillCategory, SkillDemand, SkillDetail} from '../../../dtos/skill';
import {UserDetail} from '../../../dtos/user';
import {SkillService} from '../../../services/skill/skill.service';
import {BarteringService} from '../../../services/bartering/bartering.service';
import {Router, RouterLink} from '@angular/router';
import {environment} from '../../../../environments/environment';
import {SkeletonComponent} from '../../util/skeleton/skeleton.component';

@Component({
  selector: 'app-recommend',
  standalone: true,
  imports: [
    CommonModule,
    SkeletonComponent,
    RouterLink
  ],
  templateUrl: './recommend.component.html',
  styleUrl: './recommend.component.scss',
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class RecommendComponent implements OnInit {
  // UI State
  currentStep: number = 1;
  isLoading: boolean = false;
  error: string | null = null;
  noDemandsCreated: boolean = false;
  noDemandsWithoutExchange: boolean = false;

  // Data
  selectedItem: SkillDemand | null = null;
  swipeItems: SkillDetail[] = [];
  matchedItem: SkillDetail | null = null;
  demands?: SkillDemand[];
  currentUser?: UserDetail;
  skillCategories: SkillCategory[] = [];
  exchangeChatId: string = '';
  readonly apiBaseUrl = environment.apiBaseUrl;

  // Modal State
  isMatchModalOpen: boolean = false;

  constructor(
    private userService: UserService,
    private skillsService: SkillService,
    private barteringService: BarteringService,
    public router: Router
  ) {}

  ngOnInit(): void {
    this.loadUserData();
    this.loadSkillCategories();
  }

  private loadSkillCategories(): void {
    this.skillsService.getSkillCategories().subscribe({
      next: (categories) => {
        this.skillCategories = categories;
      },
      error: (error) => {
        console.error('Error loading skill categories:', error);
        this.error = 'Failed to load skill categories';
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  private removeDemandsWithExchange(demands: SkillDemand[]): void {
    if (!demands) {
      return;
    }

    this.barteringService.getMyExchangeChats().subscribe({
      next: (chats) => {
        const exchangedSkillIds = new Set(
          chats.items.flatMap(exchange =>
            exchange.exchanges.flatMap(exchangeChat => [
              exchangeChat.exchangedSkill.id,
              exchangeChat.exchangedSkillCounterpart?.id
            ].filter((id): id is number => id !== undefined))
          ));
        let demandsFiltered = demands?.filter(
          demand => !exchangedSkillIds.has(demand.id)
        );
        this.demands = demandsFiltered;
        if (demands?.length > 0 && demandsFiltered.length <= 0) this.noDemandsWithoutExchange = true;
        if (demands?.length <= 0) {
          this.noDemandsWithoutExchange = false;
          this.noDemandsCreated = true;
        }
      },
      error: (error) => {
        console.error('Error fetching exchange chats:', error);
        this.error = 'Failed to load exchange data';
      }
    });
  }

  getCategoryNameById(categoryId: number | undefined): string {
    if (categoryId === undefined) {
      return '';
    }
    const category = this.skillCategories.find(cat => cat.id === categoryId);
    return category?.name || 'Unknown Category';
  }

  getProfileImageUrl(profilePicture: string | undefined | null): string {
    return profilePicture ? this.apiBaseUrl + '/images/' + profilePicture : 'resources/profile_icon.png';
  }

  private loadUserData(): void {
    this.isLoading = true;
    this.userService.getDetailedCurrentUser().subscribe({
      next: (user: UserDetail) => {
        this.currentUser = user;
        this.removeDemandsWithExchange(user.skillDemands);
      },
      error: (error) => {
        this.error = 'Failed to load user data';
        console.error('Error loading user data:', error);
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  private fetchSwipeItems(): void {
    this.isLoading = true;

    if (this.selectedItem === null) {
      return;
    }

    this.skillsService.getRecommendations(this.selectedItem).subscribe({
      next: (recommendations) => {
        console.log('Received recommendations: ', recommendations);
        this.swipeItems = recommendations.items;
      },
      error: (error) => {
        this.error = 'Failed to load recommendations';
        console.error('Error loading recommendations:', error);
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  // Navigation Methods
  goBack(): void {
    this.currentStep = 1;
    this.selectedItem = null;
    this.swipeItems = [];
    this.ngOnInit();
  }

  // Selection Methods
  onItemSelect(selectedItem: SkillDemand): void {
    this.selectedItem = selectedItem;
    this.currentStep = 2;
    this.fetchSwipeItems();
  }

  // Match Methods
  onMatchClick(item: SkillDetail): void {
    this.matchedItem = item;
    this.isMatchModalOpen = true;
  }

  closeMatchModal(): void {
    this.isMatchModalOpen = false;
    this.matchedItem = null;
  }

  continueBrowsing(): void {
    this.isMatchModalOpen = false;
    this.matchedItem = null;
    // Remove the selected item from swipeItems
    if (this.matchedItem && this.swipeItems) {
      this.swipeItems = this.swipeItems.filter(item => item.id !== this.matchedItem?.id);
    }
  }

  goToChat(): void {
    if (this.matchedItem && this.selectedItem) {
      this.isLoading = true;
      this.barteringService.initiateExchange({
        exchanges: [
          {
            skillID: this.matchedItem.id,
            skillCounterPartID: this.selectedItem.id
          }
        ],
        chatMessage: {
          content: `Hello! I would like to take your offer from your post ${this.matchedItem.title} for my demand ${this.selectedItem.title}. Would you want to help me? :D\nThank you!`
        }
      }).subscribe({
        next: (result) => {
          this.exchangeChatId = result.id;
          this.router.navigate(['/chat/' + this.exchangeChatId]);
          this.closeMatchModal();
        },
        error: (error) => {
          console.error('Error initiating exchange:', error);
          this.error = 'Failed to initiate exchange';
        },
        complete: () => {
          this.isLoading = false;
        }
      });
    }
  }

  // Helper Methods
  isStepComplete(step: number): boolean {
    switch (step) {
      case 1:
        return !!this.selectedItem;
      case 2:
        return !!this.matchedItem;
      default:
        return false;
    }
  }
}