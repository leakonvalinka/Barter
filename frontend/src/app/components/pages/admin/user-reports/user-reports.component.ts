import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../../services/admin/admin.service';
import { UserReportDTO, BanStatusDTO, ReportStatus } from '../../../../dtos/admin';
import {
  NgpDialog,
  NgpDialogTitle,
  NgpDialogDescription,
  NgpDialogTrigger,
  NgpDialogOverlay,
} from 'ng-primitives/dialog';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { forkJoin } from 'rxjs';
import { ConfirmationDialogService } from '../../../../services/dialog/confirmation-dialog.service';

@Component({
  selector: 'app-user-reports',
  templateUrl: './user-reports.component.html',
  styleUrls: ['./user-reports.component.scss'],
  imports: [
    NgpDialog,
    NgpDialogTitle,
    NgpDialogDescription,
    NgpDialogTrigger,
    NgpDialogOverlay,
    FormsModule,
    CommonModule,
  ],
  standalone: true
})
export class UserReportsComponent implements OnInit {
  reports: UserReportDTO[] = [];
  banReason: string = '';
  userBanStatus: Map<string, boolean> = new Map();

  constructor(
    private adminService: AdminService, 
    private router: Router,
    private toastr: ToastrService,
    private confirmationDialog: ConfirmationDialogService
  ) {}

  ngOnInit() {
    this.loadReports();
  }

  loadReports() {
    this.adminService.getAllUserReports().subscribe({
      next: (reports) => {
        this.reports = reports;
        // Get unique usernames
        const uniqueUsernames = [...new Set(reports.map(report => report.reportedUserUsername))];
        // Check ban status for each unique user
        const banStatusChecks = uniqueUsernames.map(username => 
          this.adminService.isUserBanned(username)
        );
        
        forkJoin(banStatusChecks).subscribe({
          next: (banStatuses) => {
            uniqueUsernames.forEach((username, index) => {
              this.userBanStatus.set(username, banStatuses[index].banned);
            });
          },
          error: (error) => {
            console.error('Error checking ban statuses:', error);
            this.toastr.error('Error checking ban statuses');
          }
        });
      },
      error: (error) => {
        console.error('Error loading reports:', error);
        this.toastr.error('Error loading reports');
      }
    });
  }

  isUserBanned(username: string): boolean {
    return this.userBanStatus.get(username) || false;
  }

  viewProfile(username: string) {
    this.router.navigate(['/profile', username]);
  }

  handleBanConfirm(username: string) {
    if (!this.banReason) {
      this.toastr.error('Please provide a reason for the ban');
      return;
    }

    this.adminService.banUser(username, this.banReason).subscribe({
      next: () => {
        this.banReason = '';
        this.loadReports();
        this.toastr.success('User banned successfully');
      },
      error: (error) => {
        console.error('Error banning user:', error);
        this.toastr.error('Error banning user');
      }
    });
  }

  async handleDeleteReport(reportId: number) {
    const confirmed = await this.confirmationDialog.confirm({
      title: 'Delete Report?',
      message: 'Are you sure you want to delete this report? This action cannot be undone.',
      confirmText: 'Yes, Delete',
      cancelText: 'Cancel'
    });

    if (confirmed.confirmed) {
      this.adminService.deleteUserReport(reportId).subscribe({
        next: () => {
          this.loadReports();
          this.toastr.success('Report deleted successfully');
        },
        error: (error) => {
          console.error('Error deleting report:', error);
          this.toastr.error('Error deleting report');
        }
      });
    }
  }
}
