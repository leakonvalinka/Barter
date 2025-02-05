import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastrService } from 'ngx-toastr';
import { RouterLink, Router, ActivatedRoute } from '@angular/router';
import { UserDetail } from '../../../../dtos/user';
import { UserService } from '../../../../services/user/user.service';
import { AuthenticationService } from '../../../../services/auth/auth.service';
import { environment } from '../../../../../environments/environment';
import { MapComponent } from '../../../util/map/map.component';
import { MapLocation } from '../../../../dtos/map-location';
import { NgpSwitch } from 'ng-primitives/switch';
import { NgpSwitchThumb } from 'ng-primitives/switch';
import { SkillOffer } from '../../../../dtos/skill';
import { SkillDemand } from '../../../../dtos/skill';
import { switchMap } from 'rxjs';
import { ConfirmationDialogService } from '../../../../services/dialog/confirmation-dialog.service';
import { UserRating } from '../../../../dtos/rating';
import { BarteringService } from '../../../../services/bartering/bartering.service';
import { ExchangeChat, ExchangeItem } from '../../../../dtos/bartering';
import { SkeletonComponent } from '../../../util/skeleton/skeleton.component';

type Skill = SkillOffer | SkillDemand;

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [
    RouterLink,
    CommonModule,
    MapComponent,
    NgpSwitch,
    NgpSwitchThumb,
    SkeletonComponent
  ],
  templateUrl: './user-profile.component.html',
  styleUrl: './user-profile.component.scss'
})
export class UserProfileComponent {
  // TODO dummy data for visualisation
  user: UserDetail = {
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
  };
  rating: number = 0.0;
  reviewAmount: number = 0;

  mapLocations: MapLocation[] = [];
  userCenter: google.maps.LatLngLiteral = { lat: 48.189415, lng: 16.373972 };

  showDemand = signal<boolean>(false);


  isOwnProfile: boolean = true;

  profilePicturePath: string = 'resources/profile_icon.png';

  ratings: UserRating[] = [];
  environment = environment;

  // Pagination related properties
  private readonly pageSize = 3;
  currentPage = 0;
  hasMoreRatings = false;
  isLoadingRatings = false;

  // Bartering history properties
  exchanges: ExchangeChat[] = [];
  private readonly exchangePageSize = 3;
  currentExchangePage = 0;
  hasMoreExchanges = false;
  isLoadingExchanges = false;

  // Loading states
  isProfileLoading = signal<boolean>(true);
  isSkillsLoading = signal<boolean>(true);
  isBarteringLoading = signal<boolean>(true);
  isMapLoading = signal<boolean>(true);

  constructor(
    private userService: UserService,
    private toaster: ToastrService,
    private authService: AuthenticationService,
    private barteringService: BarteringService,
    private router: Router,
    private route: ActivatedRoute,
    private confirmationDialog: ConfirmationDialogService
  ) { }

  ngOnInit() {
    this.isProfileLoading.set(true);
    this.isSkillsLoading.set(true);
    this.isBarteringLoading.set(true);
    this.isMapLoading.set(true);

    // get route username
    this.route.paramMap.pipe(
      switchMap((params) => {
        const routeUsername = params.get('username') ?? '';
        return this.userService.getDetailedUser(routeUsername);
      })
    ).subscribe({
      next: responseUser => {
        this.user = responseUser;
        if (this.user.profilePicture) this.profilePicturePath = `${environment.apiBaseUrl}/images/${this.user.profilePicture}`;
        this.isOwnProfile = this.userService.isCurrentUser(responseUser.username);
        this.userCenter = { lat: this.user.location.homeLocation.coordinates[0], lng: this.user.location.homeLocation.coordinates[1] };
        this.mapLocations = [{
          lat: this.user.location.homeLocation.coordinates[0],
          lng: this.user.location.homeLocation.coordinates[1],
          user: {
            username: this.user.username,
            displayName: this.user.displayName,
            profilePicture: this.user.profilePicture,
            rating: this.user.averageRatingHalfStars ?? 0,
            skills: this.user.skillOffers.map(skill => skill.title)
          }
        }];
        this.isProfileLoading.set(false);
        this.isSkillsLoading.set(false);
        this.isMapLoading.set(false);
        this.getRatings();
        this.getExchanges();
      },
      error: error => {
        console.error(error);
        this.toaster.error('Could not find user!');
        this.router.navigate(['explore']);
        this.isProfileLoading.set(false);
        this.isSkillsLoading.set(false);
        this.isBarteringLoading.set(false);
        this.isMapLoading.set(false);
      }
    });
  }

  get displayedSkills(): (SkillOffer | SkillDemand)[] {
    return this.showDemand() ? this.user.skillDemands : this.user.skillOffers;
  }

  async logout() {
    const confirmed = await this.confirmationDialog.confirm({
      title: 'Logout',
      message: 'Are you sure you want to logout?',
      confirmText: 'Logout',
      cancelText: 'Cancel'
    });

    if (confirmed.confirmed) {
      this.authService.logout();
      this.router.navigate(['/login']);
      this.toaster.success('Successfully logged out');
    }
  }

  getRatings() {
    this.isLoadingRatings = true;
    this.userService.getUserRatings(this.user.username, {
      page: this.currentPage,
      pageSize: this.pageSize
    }).subscribe({
      next: ratings => {
        if (this.currentPage === 0) {
          this.ratings = ratings.items;
        } else {
          this.ratings = [...this.ratings, ...ratings.items];
        }
        this.hasMoreRatings = ratings.hasMore;
        this.isLoadingRatings = false;
      },
      error: error => {
        console.error(error);
        this.toaster.error('Could not fetch ratings!');
        this.isLoadingRatings = false;
      }
    });
  }

  loadMoreRatings() {
    this.currentPage++;
    this.getRatings();
  }

  getExchanges() {
    this.isLoadingExchanges = true;
    this.isBarteringLoading.set(true);
    this.barteringService.getExchangeChatsByUsername(
      this.user.username,
      {
      page: this.currentExchangePage,
      pageSize: this.exchangePageSize
    }).subscribe({
      next: exchanges => {
        if (this.currentExchangePage === 0) {
          this.exchanges = exchanges.items;
        } else {
          this.exchanges = [...this.exchanges, ...exchanges.items];
        }
        this.hasMoreExchanges = exchanges.hasMore;
        this.isLoadingExchanges = false;
        this.isBarteringLoading.set(false);
      },
      error: error => {
        console.error(error);
        this.toaster.error('Could not fetch exchange history!');
        this.isLoadingExchanges = false;
        this.isBarteringLoading.set(false);
      }
    });
  }

  loadMoreExchanges() {
    this.currentExchangePage++;
    this.getExchanges();
  }

  getExchangeStatusClasses(item: ExchangeItem): { [key: string]: boolean } {
    const isCompleted = item.initiatorMarkedComplete && item.responderMarkedComplete;
    const isPartiallyComplete = (item.initiatorMarkedComplete || item.responderMarkedComplete) && !isCompleted;

    return {
      'bg-green-100': isCompleted,
      'text-green-700': isCompleted,
      'bg-yellow-100': isPartiallyComplete,
      'text-yellow-700': isPartiallyComplete,
      'bg-gray-100': !isCompleted && !isPartiallyComplete,
      'text-gray-700': !isCompleted && !isPartiallyComplete
    };
  }

  getExchangeStatus(item: ExchangeItem): string {
    if (item.initiatorMarkedComplete && item.responderMarkedComplete) {
      return 'Completed';
    }
    if (item.initiatorMarkedComplete || item.responderMarkedComplete) {
      return 'Partially Complete';
    }
    return 'In Progress';
  }

  getExchangePartnerName(exchange: ExchangeChat): string {
    return exchange.initiator.username === this.user.username ? 'you' : '@' + exchange.initiator.username;
  }

  getProfilePictureUrl(pictureId: string | null): string {
    return pictureId ? `${environment.apiBaseUrl}/images/${pictureId}` : 'resources/profile_icon.png';
  }
}
