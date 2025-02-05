import { Component, ElementRef, OnInit, ViewChild, Output, EventEmitter, Input, output, input } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { environment } from '../../../../../../environments/environment';
import { MapsService } from '../../../../../services/maps/maps.service';
import { UserService } from '../../../../../services/user/user.service';
import { UserAddress, UserUpdate } from '../../../../../dtos/user';
import { MapLocation } from '../../../../../dtos/map-location';
import { MapComponent } from '../../../../util/map/map.component';

const DEFAULT_PROFILE_PICTURE = 'resources/profile_icon.png';
const DEFAULT_MAP_CENTER: google.maps.LatLngLiteral = { lat: 48.189415, lng: 16.373972 }; // Vienna
const ADDRESS_DEBOUNCE_TIME = 300;
const MIN_ADDRESS_LENGTH = 3;
const MAX_BIO_LENGTH = 100;

interface PlaceDetails {
  address: any;
  location: {
    lat: number;
    lng: number;
  };
}

export interface UserInfoButtons {
  submit?: {
    text?: string;
    class?: string;
    show?: boolean;
  };
  cancel?: {
    text?: string;
    class?: string;
    show?: boolean;
  };
}


const DEFAULT_BUTTON_CONFIG: UserInfoButtons = {
  submit: {
    text: 'Save Changes',
    class: 'btn-primary',
    show: true
  },
  cancel: {
    text: 'Cancel',
    class: 'btn-secondary',
    show: true
  }
};

@Component({
  selector: 'app-user-info',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    FormsModule,
    MapComponent
  ],
  templateUrl: './user-info.component.html',
  styleUrl: './user-info.component.scss'
})
export class UserInfoComponent implements OnInit {
  @ViewChild('addressInput') private readonly addressInput!: ElementRef<HTMLInputElement>;
  userInfoUpdate = output<{ user: UserUpdate, dirty: boolean }>();
  cancel = output<void>();
  buttonConfig = input<UserInfoButtons>(DEFAULT_BUTTON_CONFIG);

  public profileForm: FormGroup;
  public user?: UserUpdate;
  public profilePicturePath: string = DEFAULT_PROFILE_PICTURE;
  public mapLocations: MapLocation[] = [];
  public mapCenter: google.maps.LatLngLiteral = DEFAULT_MAP_CENTER;
  public addressSuggestions: google.maps.places.AutocompletePrediction[] = [];
  public routeUsername: string = '';

  private isAddressBeingSelected = false;

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly userService: UserService,
    private readonly mapsService: MapsService,
    private readonly toaster: ToastrService
  ) {
    this.profileForm = this.initializeForm();
  }

  public ngOnInit(): void {
    this.loadUserData();
  }

  public onAddressSelected(prediction: google.maps.places.AutocompletePrediction): void {
    this.addressSuggestions = [];
    this.addressInput.nativeElement.blur();

    this.mapsService.getPlaceDetails(prediction.place_id).subscribe({
      next: (details) => {
        if (details) {
          this.handlePlaceDetails(details as PlaceDetails, prediction);
        }
      },
      error: (error) => this.handlePlaceError(error)
    });
  }

  public removeProfilePicture(): void {
    this.profilePicturePath = DEFAULT_PROFILE_PICTURE;
    if (this.user) {
      this.user.profilePicture = undefined;
    }
  }

  public onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) {
      this.previewImage(file);
    }
  }

  public onSubmit(): void {
    if (!this.profileForm.valid || !this.user) {
      return;
    }

    const updatedUser = this.prepareUserUpdate();
    this.userInfoUpdate.emit({ dirty: this.profileForm.dirty, user: updatedUser });
  }

  public onCancel(): void {
    this.cancel.emit();
  }

  private initializeForm(): FormGroup {
    return this.formBuilder.group({
      addressInput: ['', Validators.required],
      bio: ['', [Validators.maxLength(MAX_BIO_LENGTH)]],
      displayName: ['', Validators.required],
      location: ['', Validators.required, this.locationValidator()]
    });
  }

  private loadUserData(): void {
    this.userService.getDetailedCurrentUser().subscribe({
      next: (user) => this.handleUserData(user),
      error: (error) => this.handleUserError(error)
    });
  }

  private handleUserData(user: UserUpdate): void {
    this.user = user;
    this.updateProfilePicture();
    this.updateFormValues();
    this.initializeMapLocation();
    this.subscribeToAddressChanges();
  }

  private updateProfilePicture(): void {
    if (this.user?.profilePicture) {
      this.profilePicturePath = `${environment.apiBaseUrl}/images/${this.user.profilePicture}`;
    }
  }

  private updateFormValues(): void {
    if (!this.user) return;

    const fullAddress = this.formatFullAddress();
    this.profileForm.patchValue({
      displayName: this.user.displayName,
      bio: this.user.bio,
      addressInput: fullAddress,
      location: this.user.location
    });
  }

  private formatFullAddress(): string {
    if (!this.user?.location?.street) return '';

    return `${this.user.location.street}${this.user.location.streetNumber ? ' ' + this.user.location.streetNumber : ''
      }, ${this.user.location.postalCode} ${this.user.location.city}, ${this.user.location.country
      }`;
  }

  private initializeMapLocation(): void {
    if (this.user?.location?.homeLocation?.coordinates?.length === 2) {
      const [lat, lng] = this.user.location.homeLocation.coordinates;
      this.updateMapLocation(lat, lng);
    }
  }

  private locationValidator(): ValidatorFn {
    return async (control: AbstractControl<UserAddress>): Promise<ValidationErrors | null> => {
      if (!control.value || control.value.streetNumber === '' || control.value.postalCode === 0 || control.value.city === '' || control.value.country === '' || control.value.street === '') {
        return { "address": { value: "Address incomplete" } }
      }

      return null;
    };
  }

  private subscribeToAddressChanges(): void {
    this.profileForm.get('addressInput')?.valueChanges
      .pipe(
        debounceTime(ADDRESS_DEBOUNCE_TIME),
        distinctUntilChanged()
      )
      .subscribe(value => this.handleAddressChange(value));
  }

  private handleAddressChange(value: string): void {
    if (!this.isAddressBeingSelected && value?.length > MIN_ADDRESS_LENGTH) {
      this.mapsService.getPlacePredictions(value).subscribe(predictions => {
        this.addressSuggestions = predictions;
      });
    } else {
      this.addressSuggestions = [];
    }
  }

  private handlePlaceDetails(
    details: PlaceDetails,
    prediction: google.maps.places.AutocompletePrediction
  ): void {
    if (!this.user) return;

    this.updateUserLocation(details);
    this.updateMapLocation(details.location.lat, details.location.lng);
    this.profileForm.patchValue({ addressInput: prediction.description, location: this.user.location });
    this.toaster.success('Location found!');
    this.isAddressBeingSelected = true;
    setTimeout(() => {
      this.isAddressBeingSelected = false
    }, ADDRESS_DEBOUNCE_TIME + 10);
  }

  private updateUserLocation(details: { address: any; location: { lat: number; lng: number } }): void {
    if (!this.user) return;


    this.user.location = {
      ...this.user.location,
      ...details.address,
      postalCode: parseInt(details.address.postalCode) || 0,
      homeLocation: {
        type: 'Point',
        coordinates: [details.location.lat, details.location.lng]
      }
    };

    console.log(this.user.location);
  }

  private updateMapLocation(lat: number, lng: number): void {
    this.mapCenter = { lat, lng };
    this.mapLocations = [{
      lat,
      lng,
      user: {
        displayName: this.user?.displayName || 'Your Location',
        username: this.routeUsername,
        profilePicture: this.profilePicturePath,
        rating: 0,
        skills: []
      }
    }];
  }

  private previewImage(file: File): void {
    const reader = new FileReader();
    reader.onload = () => {
      const result = reader.result as string;
      this.profilePicturePath = result;
      if (this.user) {
        this.user.profilePicture = result.split(',')[1];
      }
    };
    reader.readAsDataURL(file);
  }

  private prepareUserUpdate(): UserUpdate {
    if (!this.user) throw new Error('User not initialized');

    const updatedUser: UserUpdate = {
      ...this.user,
      displayName: this.profileForm.get('displayName')?.value,
      bio: this.profileForm.get('bio')?.value
    };
    console.log(updatedUser);
    return updatedUser;
  }

  private handlePlaceError(error: any): void {
    console.error('Error getting place details:', error);
    this.toaster.error('Could not get location details');
  }

  private handleUserError(error: any): void {
    this.toaster.error('Could not find user!');
    this.profileForm = this.initializeForm();
    console.error(error);
  }

  private handleUpdateError(error: any): void {
    console.error('Profile update error:', error);
    this.toaster.error('Could not update profile!');
  }
}
