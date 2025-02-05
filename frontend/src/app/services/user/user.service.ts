import { HttpClient, HttpContext, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { User, UserDetail, UserUpdate, VerificationRequest } from '../../dtos/user';
import { environment } from '../../../environments/environment';
import { BYPASS_AUTH } from '../../interceptors/http.interceptor';
import { jwtDecode } from 'jwt-decode';
import { AuthenticationService } from '../auth/auth.service';
import {PaginatedResults, PaginationParams, paginationToHttpQueryParams} from '../../dtos/pagination';
import {UserRating} from '../../dtos/rating';

@Injectable({
  providedIn: 'root'
})
/**
 * Service for handling user registration requests to the backend.
 */
export class UserService {

  private userEndpointUrl = `${environment.apiBaseUrl}/users`;

  constructor(
    private http: HttpClient,
    private authService: AuthenticationService
  ) { }

  /**
   * Requests details of the currently authenticated user
   * @return Observable<UserDetail> details about the currently authenticated user
   * (here, private fields like email will actually be set)
   */
  getDetailedCurrentUser(): Observable<UserDetail> {
    return this.http.get<UserDetail>(this.userEndpointUrl);
  }

  /**
   * Requests details of the user with the provided username
   * @return Observable<UserDetail> the user that matches the given id
   * (note that private information like email will not be set)
   */
  getDetailedUser(username: string): Observable<UserDetail> {
    if (this.isCurrentUser(username)) {
      return this.getDetailedCurrentUser();
    }

    return this.http.get<UserDetail>(this.userEndpointUrl + "/" + username);
  }

  /**
   * Updates the information displayed in the users profile
   * @param user the updated information on the user
   */
  updateUserInformation(user: UserUpdate): Observable<UserDetail> {
    return this.http.put<UserDetail>(this.userEndpointUrl, user);
  }

  /**
   * decodes the authentication token to obtain the logged-in user's username
   * @return the username or null, should the token be null or not contain a username
   */
  extractUsernameFromToken(): string | null {
    const currentUser = this.authService.getCurrentDecodedUserToken();
    return currentUser?.sub;
  }

  isCurrentUser(username: string): boolean {
    const currentUser = this.authService.getCurrentDecodedUserToken();
    return currentUser?.sub === username;
  }

  /**
   * Requests ratings for a given user by their username
   * @return Observable<PaginatedResults<UserRating>> paginated results containing ratings for the user
   */
  getUserRatings(username: string, paginationParams: PaginationParams = {page: 0, pageSize: 50}): Observable<PaginatedResults<UserRating>>{
    return this.http.get<PaginatedResults<UserRating>>(this.userEndpointUrl + '/' + username + '/ratings', {
      params: paginationToHttpQueryParams(paginationParams)
    });
  }
}
