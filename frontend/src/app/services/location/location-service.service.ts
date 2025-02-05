import { Injectable } from '@angular/core';
import { Observable, from, of } from 'rxjs';
import { map } from 'rxjs/operators';

/**
 * Service for handling location-related operations including geocoding and distance calculations.
 * This service provides functionality to:
 * - Convert addresses to coordinates (geocoding)
 * - Calculate distances between two points using the Haversine formula
 */
@Injectable({
  providedIn: 'root'
})
export class LocationServiceService {

  constructor() { }

  /**
   * Calculates the distance between two points on Earth using the Haversine formula.
   * The Haversine formula determines the great-circle distance between two points on a sphere
   * given their latitudes and longitudes.
   * 
   * @param lat1Deg - Latitude of the first point in degrees
   * @param lon1Deg - Longitude of the first point in degrees
   * @param lat2Deg - Latitude of the second point in degrees
   * @param lon2Deg - Longitude of the second point in degrees
   * @returns Distance between the points in kilometers
   */
  haversineDistanceKM(lat1Deg: number, lon1Deg: number, lat2Deg: number, lon2Deg: number) {
    // Convert degrees to radians for trigonometric calculations
    const toRad = (degree: number) => {
        return degree * Math.PI / 180;
    }
    
    const lat1 = toRad(lat1Deg);
    const lon1 = toRad(lon1Deg);
    const lat2 = toRad(lat2Deg);
    const lon2 = toRad(lon2Deg);
    
    const { sin, cos, sqrt, atan2 } = Math;
    
    const R = 6371; // Earth's radius in kilometers
    const dLat = lat2 - lat1;
    const dLon = lon2 - lon1;

    // Haversine formula
    const a = sin(dLat / 2) * sin(dLat / 2)
            + cos(lat1) * cos(lat2)
            * sin(dLon / 2) * sin(dLon / 2);
    const c = 2 * atan2(sqrt(a), sqrt(1 - a)); 
    const d = R * c;
    return d; // Returns distance in kilometers
  }
}
