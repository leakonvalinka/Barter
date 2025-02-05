import { Injectable } from '@angular/core';
import { from, Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class MapsService {
  private geocoder: google.maps.Geocoder | null = null;
  private placesAutocomplete: google.maps.places.AutocompleteService | null = null;
  private placesService: google.maps.places.PlacesService | null = null;

  constructor() { 
    this.initServices();
  }

  private async initServices() {
    try {
      await google.maps.importLibrary("places");
      await google.maps.importLibrary("geocoding");
      
      this.geocoder = new google.maps.Geocoder();
      this.placesAutocomplete = new google.maps.places.AutocompleteService();
      
      // PlacesService needs a DOM element to initialize
      const mapDiv = document.createElement('div');
      const map = new google.maps.Map(mapDiv);
      this.placesService = new google.maps.places.PlacesService(map);
    } catch (error) {
      console.error('Error initializing Google Maps services:', error);
    }
  }

  getPlacePredictions(input: string): Observable<google.maps.places.AutocompletePrediction[]> {
    if (!this.placesAutocomplete) {
      console.error('Places Autocomplete not initialized');
      return of([]);
    }

    return from(
      this.placesAutocomplete.getPlacePredictions({
        input,
        componentRestrictions: { country: ['at', 'de'] }, // Restrict to Austria and Germany
        types: ['address']
      })
    ).pipe(
      map(response => response.predictions)
    );
  }

  getPlaceDetails(placeId: string): Observable<{
    address: {
      street: string;
      streetNumber: string;
      city: string;
      postalCode: string;
      country: string;
    };
    location: google.maps.LatLngLiteral;
  } | null> {
    if (!this.placesService) {
      console.error('Places Service not initialized');
      return of(null);
    }

    return from(
      new Promise<{
        address: {
          street: string;
          streetNumber: string;
          city: string;
          postalCode: string;
          country: string;
        };
        location: google.maps.LatLngLiteral;
      } | null>((resolve, reject) => {
        this.placesService!.getDetails(
          {
            placeId: placeId,
            fields: ['address_components', 'geometry']
          },
          (result, status) => {
            if (status === google.maps.places.PlacesServiceStatus.OK && result) {
              const addressComponents = result.address_components || [];
              const address = {
                street: '',
                streetNumber: '',
                city: '',
                postalCode: '',
                country: ''
              };

              // Map address components to our format
              addressComponents.forEach(component => {
                const types = component.types;
                if (types.includes('route')) {
                  address.street = component.long_name;
                } else if (types.includes('street_number')) {
                  address.streetNumber = component.long_name;
                } else if (types.includes('locality')) {
                  address.city = component.long_name;
                } else if (types.includes('postal_code')) {
                  address.postalCode = component.long_name;
                } else if (types.includes('country')) {
                  address.country = component.long_name;
                }
              });

              resolve({
                address,
                location: {
                  lat: result.geometry?.location?.lat() || 0,
                  lng: result.geometry?.location?.lng() || 0
                }
              });
            } else {
              reject(new Error('Failed to get place details'));
            }
          }
        );
      })
    );
  }

  /**
   * Converts a full address string into geographic coordinates using Google's Geocoding service.
   * 
   * @param address - Complete address string to geocode
   * @returns Observable containing the latitude and longitude, or null if geocoding fails
   */
  geocodeAddress(address: string): Observable<google.maps.LatLngLiteral | null> {
    if (!this.geocoder) {
      console.error('Geocoder not initialized');
      return of(null);
    }

    return from(
      this.geocoder.geocode({
        address: address
      })
    ).pipe(
      map(response => {
        if (response.results.length > 0) {
          const location = response.results[0].geometry.location;
          return {
            lat: location.lat(),
            lng: location.lng()
          };
        }
        return null;
      })
    );
  }

  /**
   * Converts address components into geographic coordinates using Google's Geocoding service.
   * This method combines the individual address components into a single string and performs geocoding.
   * 
   * @param street - Street name
   * @param streetNumber - Street number or building number
   * @param city - City name
   * @param postalCode - Postal/ZIP code
   * @param country - Country name
   * @returns Observable containing the latitude and longitude, or null if geocoding fails
   */
  geocodeAddressFromComponents(
    street: string,
    streetNumber: string,
    city: string,
    postalCode: string,
    country: string
  ): Observable<google.maps.LatLngLiteral | null> {
    const fullAddress = `${street} ${streetNumber}, ${postalCode} ${city}, ${country}`;
    return this.geocodeAddress(fullAddress);
  }
}
