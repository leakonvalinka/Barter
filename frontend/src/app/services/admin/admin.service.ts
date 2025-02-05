import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserBanDTO, BanStatusDTO, UserReportDTO, SkillReportDTO, ReportStatus } from '../../dtos/admin';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = `${environment.apiBaseUrl}/admin`;

  constructor(private http: HttpClient) { }

  /**
   * Ban a user permanently.
   * @param username Username of the user to ban
   * @param reason Reason for the ban
   * @returns Observable of the created ban
   */
  banUser(username: string, reason: string): Observable<UserBanDTO> {
    return this.http.post<UserBanDTO>(`${this.apiUrl}/ban/${username}`, null, {
      params: { reason }
    });
  }

  /**
   * Check if a user is banned.
   * @param username Username of the user to check
   * @returns Observable of the ban status
   */
  isUserBanned(username: string): Observable<BanStatusDTO> {
    return this.http.get<BanStatusDTO>(`${this.apiUrl}/ban/${username}`);
  }

  /**
   * Report a user for inappropriate behavior.
   * @param reportedUserUsername Username of the user being reported
   * @param reason Reason for the report
   * @returns Observable of the created report
   */
  reportUser(reportedUserUsername: string, reason: string): Observable<UserReportDTO> {
    return this.http.post<UserReportDTO>(`${this.apiUrl}/reports/users/${reportedUserUsername}`, null, {
      params: { reason }
    });
  }

  /**
   * Get all reports for a specific user.
   * @param username Username of the user to get reports for
   * @returns Observable of user reports
   */
  getUserReports(username: string): Observable<UserReportDTO[]> {
    return this.http.get<UserReportDTO[]>(`${this.apiUrl}/reports/users/${username}`);
  }

  /**
   * Get all pending user reports.
   * @returns Observable of pending user reports
   */
  getPendingUserReports(): Observable<UserReportDTO[]> {
    return this.http.get<UserReportDTO[]>(`${this.apiUrl}/reports/users/pending`);
  }

  /**
   * Get all user reports.
   * @returns Observable of all user reports
   */
  getAllUserReports(): Observable<UserReportDTO[]> {
    return this.http.get<UserReportDTO[]>(`${this.apiUrl}/reports/users`);
  }

  /**
   * Report a skill for inappropriate content.
   * @param skillId ID of the skill being reported
   * @param reason Reason for the report
   * @returns Observable of the created report
   */
  reportSkill(skillId: number, reason: string): Observable<SkillReportDTO> {
    return this.http.post<SkillReportDTO>(`${this.apiUrl}/reports/skills/${skillId}`, null, {
      params: { reason }
    });
  }

  /**
   * Get all reports for a specific skill.
   * @param skillId ID of the skill to get reports for
   * @returns Observable of skill reports
   */
  getSkillReports(skillId: number): Observable<SkillReportDTO[]> {
    return this.http.get<SkillReportDTO[]>(`${this.apiUrl}/reports/skills/${skillId}`);
  }

  /**
   * Get all pending skill reports.
   * @returns Observable of pending skill reports
   */
  getPendingSkillReports(): Observable<SkillReportDTO[]> {
    return this.http.get<SkillReportDTO[]>(`${this.apiUrl}/reports/skills/pending`);
  }

  /**
   * Get all skill reports.
   * @returns Observable of all skill reports
   */
  getAllSkillReports(): Observable<SkillReportDTO[]> {
    return this.http.get<SkillReportDTO[]>(`${this.apiUrl}/reports/skills`);
  }

  /**
   * Update the status of a skill report.
   * @param reportId ID of the report to update
   * @param newStatus New status to set
   * @returns Observable of the updated report
   */
  updateSkillReportStatus(reportId: number, newStatus: ReportStatus): Observable<SkillReportDTO> {
    return this.http.put<SkillReportDTO>(`${this.apiUrl}/reports/skills/${reportId}/status`, null, {
      params: { status: newStatus }
    });
  }


  /**
   * Delete a user report.
   * @param reportId ID of the report to delete
   * @returns Observable of void
   */
  deleteUserReport(reportId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/reports/users/${reportId}`);
  }
}
