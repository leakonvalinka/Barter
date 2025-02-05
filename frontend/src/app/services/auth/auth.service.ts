import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap, switchMap, catchError, throwError, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { jwtDecode } from 'jwt-decode';
import { BYPASS_AUTH } from '../../interceptors/http.interceptor';
import { User, VerificationRequest } from '../../dtos/user';
import { LoginResponseDTO } from '../../dtos/auth';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  private baseUrl = `${environment.apiBaseUrl}/auth`;
  private registerUrl = `${this.baseUrl}/register`;
  private loginUrl = `${this.baseUrl}/login`;
  private verifyUrl = `${this.baseUrl}/verify`;
  private refreshTokenUrl = `${this.baseUrl}/refresh-token`;

  private authState = new BehaviorSubject<{
    isAuthenticated: boolean;
    role: string | null;
    token: string | null;
  }>({
    isAuthenticated: false,
    role: null,
    token: null
  });

  constructor(private http: HttpClient) {
    const token = localStorage.getItem('authToken');
    if (this.isTokenValid(token)) {
      const role = this.extractKeyFromToken<string[]>(token!, 'groups')?.[0] ?? 'USER';
      this.authState.next({
        isAuthenticated: true,
        role,
        token
      });
    } else {
      this.refreshAccessToken().subscribe();
    }
  }


  /**
   * Registers a new user by sending email and password to the server.
   * @param email User's email address for registration.
   * @param password User's password for registration.
   * @returns Observable<any> Response observable from the server.
   */
  registerUser(user: User): Observable<any> {
    return this.http.post<any>(this.registerUrl, user, { context: new HttpContext().set(BYPASS_AUTH, true) });
  }

  verifyUser(verificationRequest: VerificationRequest): Observable<LoginResponseDTO> {
    return this.http.post<LoginResponseDTO>(this.verifyUrl, verificationRequest, { context: new HttpContext().set(BYPASS_AUTH, true) }).pipe(
      tap(response => {
        if (response.jwt) {
          localStorage.setItem('authToken', response.jwt);
          localStorage.setItem('refreshToken', response.refreshToken);
          const role = this.extractKeyFromToken<string[]>(response.jwt, 'groups')?.[0] ?? 'USER';
          this.authState.next({
            isAuthenticated: true,
            role,
            token: response.jwt
          });
        }
      })
    );

  }

  /**
   * Logs in a user by sending email and password to the server.
   *
   * @param credentials Object containing email and password for login.
   * @returns Observable<any> Response observable from the server, typically with a token or user data.
   */
  loginUser(credentials: { emailOrUsername: string, password: string }): Observable<LoginResponseDTO> {
    return this.http.post<any>(this.loginUrl, credentials, { context: new HttpContext().set(BYPASS_AUTH, true) }).pipe(
      tap(response => {
        if (response.jwt) {
          localStorage.setItem('authToken', response.jwt);
          localStorage.setItem('refreshToken', response.refreshToken);
          const role = this.extractKeyFromToken<string[]>(response.jwt, 'groups')?.[0] ?? 'USER';
          this.authState.next({
            isAuthenticated: true,
            role,
            token: response.jwt
          });
        }
      })
    );
  }

  /**
   * Attempts to refresh the access token using the refresh token
   * @returns Observable<any> New tokens from the server
   */
  refreshAccessToken(): Observable<any> {
    const token = localStorage.getItem('authToken');
    const refreshToken = localStorage.getItem('refreshToken');

    // If no refresh token, return an observable that completes immediately
    if (!refreshToken) {
      return of(null);
    }

    // Optional: Add token expiration check
    if (token && this.isTokenValid(token)) {
      return of(null); // Token is still valid, no need to refresh
    }

    return this.http.post<any>(
      this.refreshTokenUrl,
      { refreshToken },
      { context: new HttpContext().set(BYPASS_AUTH, true) }
    ).pipe(
      tap(response => {
        if (response?.jwt) {
          localStorage.setItem('authToken', response.jwt);
          localStorage.setItem('refreshToken', response.refreshToken);
          const role = this.extractKeyFromToken<string[]>(response.jwt, 'groups')?.[0] ?? 'USER';
          this.authState.next({
            isAuthenticated: true,
            role,
            token: response.jwt
          });
        }
      })
    );
  }

  /**
   * Clears the JWT token from localStorage, effectively logging out the user.
   */
  logout(): void {
    localStorage.removeItem('authToken');
    localStorage.removeItem('refreshToken');
    this.authState.next({
      isAuthenticated: false,
      role: null,
      token: null
    });
  }

  /**
   * Extracts a specific key from a JWT token's payload.
   *
   * @param token The JWT token to decode
   * @param key The key to extract from the decoded token
   * @returns T | null The extracted value or null if not found
   */
  private extractKeyFromToken<T>(token: string, key: string): T | null {
    try {
      const decoded: any = jwtDecode(token);
      return decoded[key] ?? null;
    } catch {
      return null;
    }
  }

  /**
   * Public getters for auth state
   *
   * @returns Observable<{ isAuthenticated: boolean; role: string | null }> Authentication state as an observable.
   */
  getAuthState(): Observable<{ isAuthenticated: boolean; role: string | null, token: string | null }> {
    return this.authState.asObservable();
  }

  /**
   * Checks if the user has the required role.
   *
   * @param requiredRole Role to check against.
   * @returns boolean True if the user has the required role.
   */
  hasRole(requiredRole: string): boolean {
    return this.authState.value.role === requiredRole;
  }

  /**
   * Checks if a JWT token is valid and not expired.
   *
   * @param token The JWT token to validate
   * @returns boolean True if the token exists and is not expired
   */
  private isTokenValid(token: string | null): boolean {
    if (!token) return false;

    try {
      const expirationTime = this.extractKeyFromToken<number>(token, 'exp');
      if (!expirationTime) return false;

      // exp is in seconds, Date.now() is in milliseconds
      return (expirationTime * 1000) > Date.now();
    } catch {
      return false;
    }
  }

  getCurrentDecodedUserToken(): any {
    const token = localStorage.getItem('authToken');
    if (!token) return null;

    try {
      return jwtDecode(token);
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  }


  /**
   * Validates a JWT token, extracts the user's role, and updates the authentication state.
   *
   * This method checks if the provided token is valid (not expired and properly formatted),
   * extracts the user's role from the token, and updates the application's authentication state.
   * If the token is invalid or the role cannot be determined, the authentication state is reset.
   *
   * @param token The JWT token to validate and process.
   * @returns {boolean} True if the token is valid and the authentication state was successfully updated,
   *                    false otherwise.
   */
  public validateAndSetAuthentication(jwtToken: string): boolean {
    if (!this.isTokenValid(jwtToken)) return false;

    const role = this.extractKeyFromToken<string[]>(jwtToken, 'groups')?.[0] ?? null;
    if (role) {
      this.authState.next({
        isAuthenticated: true,
        role,
        token: jwtToken
      });
      localStorage.setItem('authToken', jwtToken);
      return true;
    }

    this.authState.next({
      isAuthenticated: false,
      role: null,
      token: null
    });
    return false;
  }

}

