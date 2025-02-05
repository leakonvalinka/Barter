import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PasswordRecoveryService {

  private resetLinkUrl = `${environment.apiBaseUrl}/auth/reset-password`;

  constructor(private http: HttpClient) { }

  /**
   * Sends a reset email to the user's email address.
   * @param email The user's email address.
   * @returns An observable that emits the response from the server.
   */
  sendResetEmail(email: string): Observable<any> {
    return this.http.post<any>(this.resetLinkUrl, { email });
  }

  /**
   * Resets the user's password using the provided token and new password.
   * @param token The reset token.
   * @param password The new password.
   * @returns An observable that emits the response from the server.
   */
  resetPassword(resetToken: string, password: any): Observable<any> {
    return this.http.post<any>(`${this.resetLinkUrl}/${resetToken}`, { password });
  }
}
