import { Component, Input, OnInit, SimpleChanges, ViewChild, ContentChild, TemplateRef } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { GoogleMapsModule, MapCircle, MapInfoWindow } from '@angular/google-maps';
import { MapLocation } from '../../../dtos/map-location';
import { environment } from '../../../../environments/environment';

interface CircleInformation {
  circleOptions: google.maps.CircleOptions;
  location: MapLocation;
}

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [GoogleMapsModule, MapCircle, MapInfoWindow, CommonModule],
  templateUrl: './map.component.html',
  styleUrl: './map.component.scss'
})
export class MapComponent implements OnInit {
  @ContentChild('headerTemplate') headerTemplate?: TemplateRef<any>;
  @Input() title: string = '';
  @Input() locations: MapLocation[] | undefined = [];
  @Input() height: string = '600px';
  @Input() center: google.maps.LatLngLiteral = { lat: 48.189415, lng: 16.373972 };
  circles: CircleInformation[] = [];
  @ViewChild(MapInfoWindow) infoWindow!: MapInfoWindow;


  circleCenter: google.maps.LatLngLiteral = { lat: 48.189415, lng: 16.373972 };
  radius = 3;

  options: google.maps.MapOptions = {
    center: this.center,
    styles: [
      {
        featureType: 'all',
        elementType: 'geometry',
        stylers: [{ color: '#f5f3ff' }] // Light purple background
      },
      {
        featureType: 'water',
        elementType: 'geometry',
        stylers: [{ color: '#e9d5ff' }] // Lighter purple for water
      },
      {
        featureType: 'road',
        elementType: 'geometry',
        stylers: [{ color: '#ffffff' }] // White roads
      },
      {
        featureType: 'poi',
        elementType: 'all',
        stylers: [{ visibility: 'off' }] // Hide all POIs
      },
      {
        featureType: 'transit',
        elementType: 'labels.text.fill',
        stylers: [{ color: '#7e22ce' }] // Purple text for transit
      },
      {
        featureType: 'administrative',
        elementType: 'labels.text.fill',
        stylers: [{ color: '#6b21a8' }] // Darker purple for administrative labels
      }
    ],
    zoom: 16,
    disableDefaultUI: true, // Removes default UI elements
    zoomControl: true, // Add back zoom controls
    fullscreenControl: true, // Add back fullscreen control
    streetViewControl: false,
  };

  private map?: google.maps.Map;

  constructor(private router: Router) { }

  ngOnInit() {
    this.createCircles();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['locations']) {
      this.createCircles();
    }
    if (changes['center']) {
      this.options = {
        ...this.options,
        center: this.center
      };
    }
  }

  private createCircles() {
    this.circles = this.locations?.map(location => {
      // TODO: uncomment this when backend is ready
      if(location.user.profilePicture) {
        location.user.profilePicture = `${environment.apiBaseUrl}/images/${location.user.profilePicture.split('/').pop()}`;
      } else {
        location.user.profilePicture = `resources/profile_icon.png`;
      }

      return {
        circleOptions: {
          center: location,
          radius: 80, // radius in meters
          fillColor: '#7e22ce', // purple fill
          fillOpacity: 0.35,
          strokeColor: '#6b21a8', // darker purple border
          strokeOpacity: 0.8,
          strokeWeight: 2,
          clickable: true,
        },
        location: location
      }
    }) ?? [];
  }

  onMapInitialized(map: google.maps.Map) {
    this.map = map;
  }

  onCircleClick(event: google.maps.MapMouseEvent, circle: CircleInformation) {
    const rating = ((circle.location.user.rating ?? 0)/2).toFixed(1);
    const reviewCount = 0; // Replace with actual review count when available
    const skills = circle.location.user.skills
      .map(skill => `${skill}`)
      .join(', ');

      //`${environment.apiBaseUrl}/images/${participants[0].profilePicture}` : `resources/profile_icon.png`,

    const profilePicture = circle.location.user.profilePicture;

    const content = `
      <div class="px-2 max-w-[250px] py-0">
        <div class="flex flex-col items-center gap-4 mb-3 sm:flex-row">
          <img src="${profilePicture}"
               class="w-16 h-16 rounded-full object-cover hidden sm:block"
               alt="${circle.location.user.displayName}">
          <div>
            <h2 class="text-2xl font-semibold text-purple-800 cursor-pointer hover:text-purple-600"
                onclick="window.dispatchEvent(new CustomEvent('navigateToProfile', { detail: '${circle.location.user.username}' }))">${circle.location.user.displayName}</h2>
            <div class="flex items-center">
              <i class="text-yellow-500 material-icons-outlined mr-1 star-icon">star</i>
              ${rating}/5 (${reviewCount} reviews)
            </div>
          </div>
        </div>
        <div class="flex flex-wrap gap-2">
          ${skills}
        </div>
      </div>
    `;

    this.infoWindow.infoWindow?.setContent(content);
    this.infoWindow.infoWindow?.setPosition(event.latLng);
    this.infoWindow.infoWindow?.open(this.map);

    // Add event listener for the custom navigation event
    window.addEventListener('navigateToProfile', ((e: CustomEvent) => {
      this.router.navigate(['/profile', e.detail]);
    }) as EventListener);
  }
}
