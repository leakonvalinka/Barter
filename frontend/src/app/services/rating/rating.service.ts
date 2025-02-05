import {Injectable} from '@angular/core';
import {environment} from '../../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {CreateUserRating, UserRating} from '../../dtos/rating';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
/**
 * Service for handling user ratings and reviews
 */
export class RatingService {

  private ratingEndpointUrl = `${environment.apiBaseUrl}/rating`;

  constructor(
    private http: HttpClient
  ) {
  }

  /**
   * sends an update for an existing rating to the backend
   * @return Observable<UserRating> updated rating data
   */
  updateRating(id: number, newRating: CreateUserRating): Observable<UserRating>{
    return this.http.put<UserRating>(this.ratingEndpointUrl + '/' + id, newRating);
  }

  /**
   * sends a request to the backend to delete a rating
   */
  deleteRating(id: number): Observable<void>{
    return this.http.delete<void>(this.ratingEndpointUrl + '/' + id);
  }
}
