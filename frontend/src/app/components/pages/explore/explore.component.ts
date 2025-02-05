import { Component, signal, Signal, computed, effect } from '@angular/core';
import { MapLocation } from '../../../dtos/map-location';
import { MapComponent } from '../../util/map/map.component';
import { SkillService } from '../../../services/skill/skill.service';
import { PillComponent } from '../../util/pill/pill.component';
import { NgpSwitch, NgpSwitchThumb } from 'ng-primitives/switch';
import { NgpSelect } from 'ng-primitives/select';
import { FormsModule } from '@angular/forms';
import { SkeletonComponent } from '../../util/skeleton/skeleton.component';

import { provideIcons } from "@ng-icons/core";
import {
  heroChevronDoubleLeft,
  heroChevronDoubleRight,
  heroChevronLeft,
  heroChevronRight,
} from "@ng-icons/heroicons/outline";
import {
  NgpSlider,
  NgpSliderRange,
  NgpSliderThumb,
  NgpSliderTrack,
} from "ng-primitives/slider";

import { SkillDetail, SkillCategory, UserLocation } from '../../../dtos/skill';
import { UserService } from '../../../services/user/user.service';
import { UserDetail } from '../../../dtos/user';
import { CommonModule } from '@angular/common';
import { toSignal, toObservable } from '@angular/core/rxjs-interop';
import { debounceTime, map, switchMap, tap } from 'rxjs/operators';
import { RouterModule } from '@angular/router';
import { LocationServiceService } from '../../../services/location/location-service.service';
import { Observable } from 'rxjs';
import {PaginatedResults} from '../../../dtos/pagination';
import { PaginationComponent } from '../../util/pagination/pagination.component';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-explore',
  imports: [
    MapComponent,
    PillComponent,
    NgpSwitch,
    NgpSwitchThumb,
    NgpSelect,
    FormsModule,
    NgpSlider,
    NgpSliderRange,
    NgpSliderThumb,
    NgpSliderTrack,
    CommonModule,
    RouterModule,
    SkeletonComponent,
    PaginationComponent
  ],
  viewProviders: [
    provideIcons({
      heroChevronDoubleLeft,
      heroChevronDoubleRight,
      heroChevronLeft,
      heroChevronRight,
    }),
  ],
  templateUrl: './explore.component.html',
  styleUrl: './explore.component.scss'
})
export class ExploreComponent {

  // TODO: add sorting but we have to wait for the backend to support it
  // TODO: add filtering but we have to wait for the backend to support it

  skillCategories: SkillCategory[] = [];
  selectedCategory = signal<SkillCategory | null>(null);
  selectedCategoryMapped = computed(() => {
    const cat = this.selectedCategory();
    return cat ? [cat.id] : undefined;
  });
  lastSelectedCategory: SkillCategory | null = null;

  showDemand = signal<boolean>(false);
  lastRequestDemand: boolean = false;
  page = signal<number>(1);
  private skillListings$: Observable<SkillDetail[]>;
  skillListings: Signal<SkillDetail[] | undefined>;
  totalItems = signal<number>(0);
  pageSize = 12;
  protected readonly Math = Math;
  locations: Signal<MapLocation[] | undefined>;
  private locations$: Observable<MapLocation[]>;
  user: UserDetail | null = null;
  sliderValue = signal<number>(60);
  debouncedSliderValue: Signal<number>;
  center: { lat: number; lng: number; } = { lat: 48.189415, lng: 16.373972 };

  isLoading = signal<boolean>(true);
  isCategoriesLoading = signal<boolean>(true);
  isSkillsLoading = signal<boolean>(true);

  previousListings: SkillDetail[] | undefined = undefined;
  isInitialLoad = signal<boolean>(true);

  constructor(
    private skillService: SkillService,
    private userService: UserService,
    private locationService: LocationServiceService
  ) {
    this.debouncedSliderValue = toSignal(
      toObservable(this.sliderValue).pipe(
        debounceTime(300)
      ),
      { initialValue: 60 }
    );

    effect(() => {
      if (this.lastRequestDemand !== this.showDemand() || this.selectedCategory()?.id !== this.lastSelectedCategory?.id) {
        this.page.set(1);
        this.isSkillsLoading.set(true);
      }

      this.lastRequestDemand = this.showDemand();
      this.lastSelectedCategory = this.selectedCategory();
    })

    this.locations$ = toObservable(computed(() => {
      const showDemand = this.showDemand();
      return {
        showDemand,
        category: this.selectedCategoryMapped(),
        lat: this.user?.location.homeLocation.coordinates[0],
        lng: this.user?.location.homeLocation.coordinates[1],
        radius: Math.round(this.debouncedSliderValue() * 1000),
        page: 0,
        pageSize: 100000
      };
    })).pipe(
      tap(() => this.isLoading.set(true)),
      switchMap(({ showDemand, category, lat, lng, page, pageSize, radius }) => {
        if(showDemand) {
          return this.skillService.getDemands(category, lat, lng, radius, {page, pageSize})
        } else {
          return this.skillService.getOffers(category, lat, lng, radius, {page, pageSize})
        }
      }),
      map(this.mapSkillsToLocations),
      tap(() => this.isLoading.set(false))
    )

    this.locations = toSignal(this.locations$);

    this.skillListings$ = toObservable(computed(() => {
      console.log("radius", this.debouncedSliderValue());
      const showDemand = this.showDemand();
      return {
        showDemand,
        category: this.selectedCategoryMapped(),
        lat: this.user?.location.homeLocation.coordinates[0],
        lng: this.user?.location.homeLocation.coordinates[1],
        page: this.page()-1,
        pageSize: this.pageSize,
        radius: Math.round(this.debouncedSliderValue() * 1000)
      };
    })).pipe(
      tap(() => {
        if (!this.isInitialLoad()) {
          this.previousListings = this.skillListings();
        }
        this.isSkillsLoading.set(true);
      }),
      switchMap(({ showDemand, category, lat, lng, page, pageSize }) => {
        if(showDemand) {
          return this.skillService.getDemands(category, lat, lng, Math.round(this.debouncedSliderValue() * 1000), {page, pageSize})
        } else {
          return this.skillService.getOffers(category, lat, lng, Math.round(this.debouncedSliderValue() * 1000), {page, pageSize})
        }
      }),
      tap(({ total }) => {
        this.totalItems.set(total);
      }),
      map(({ items }) => items),
      tap(() => {
        this.isSkillsLoading.set(false);
        this.isInitialLoad.set(false);
        this.previousListings = undefined;
      })
    )
    this.skillListings = toSignal(this.skillListings$)
  }

  async ngOnInit(): Promise<void> {
    this.isCategoriesLoading.set(true);
    this.isSkillsLoading.set(true);
    this.isLoading.set(true);

    try {
      const categories = await this.skillService.getSkillCategories().toPromise();
      this.skillCategories = categories || [];
    } finally {
      this.isCategoriesLoading.set(false);
    }

    this.userService.getDetailedCurrentUser().subscribe({
      next: (user) => {
        this.user = user;
          this.center = {
            lat: user.location.homeLocation.coordinates[0],
            lng: user.location.homeLocation.coordinates[1]
          };
      },
      complete: () => {
        this.isLoading.set(false);
      }
    });
  }

  onCategoryChange(category: SkillCategory | null): void {
    if (this.selectedCategory() === category) {
      category = null;
    }

    this.selectedCategory.set(category);
  }

  calculateDistanceToCurrentUser(location: UserLocation): number {
    if (!this.user) return 0;
    return this.locationService
      .haversineDistanceKM(
        this.user.location.homeLocation.coordinates[0],
        this.user.location.homeLocation.coordinates[1],
        location.homeLocation.coordinates[0],
        location.homeLocation.coordinates[1]
      );
  }

  getCategoryName(categoryID: number): string {
    return this.skillCategories.find(category => category.id === categoryID)?.name || '';
  }

  private mapSkillsToLocations(response: PaginatedResults<SkillDetail>): MapLocation[] {
    const userSkillMap = response.items.reduce((map, skill) => {
      const userId = skill.byUser.username;
      if (!map.has(userId)) {
        map.set(userId, {
          user: skill.byUser,
          skills: new Set<string>()
        });
      }
      map.get(userId)!.skills.add(skill.title);
      return map;
    }, new Map<string, { user: any, skills: Set<string> }>());

    // Convert grouped data to locations array
    console.log("UserSkillMap: ", userSkillMap);
    return Array.from(userSkillMap.values()).map(({ user, skills }) => ({
        lat: user.location.homeLocation.coordinates[0],
        lng: user.location.homeLocation.coordinates[1],
        user: {
          displayName: user.displayName,
          profilePicture: user.profilePicture,
          rating: user.averageRatingHalfStars, // TODO: Get rating
          skills: Array.from(skills), // Convert Set back to array
          username: user.username
        }
    })).filter(location => location.lat && location.lng);

  }
}

