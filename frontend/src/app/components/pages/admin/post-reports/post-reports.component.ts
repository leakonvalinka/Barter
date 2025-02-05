import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../../services/admin/admin.service';
import { SkillReportDTO, ReportStatus } from '../../../../dtos/admin';
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

@Component({
  selector: 'app-post-reports',
  templateUrl: './post-reports.component.html',
  styleUrls: ['./post-reports.component.scss'],
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
export class PostReportsComponent implements OnInit {
  reports: SkillReportDTO[] = [];
  deleteReason: string = '';

  constructor(
    private adminService: AdminService,
    private router: Router,
    private toastr: ToastrService
  ) {}

  ngOnInit() {
    this.loadReports();
  }

  loadReports() {
    this.adminService.getAllSkillReports().subscribe({
      next: (reports) => {
        this.reports = reports;
      },
      error: (error) => {
        console.error('Error loading reports:', error);
        this.toastr.error('Error loading reports');
      }
    });
  }

  viewSkill(report: SkillReportDTO) {
    const route = report.skillType === 'DEMAND' ? '/demands' : '/offers';
    this.router.navigate([route, report.skillId]);
  }

  viewProfile(username: string) {
    this.router.navigate(['/profile', username]);
  }

  handleDeleteConfirm(reportId: number) {
    if (!this.deleteReason) {
      this.toastr.error('Please provide a reason for deletion');
      return;
    }

    this.adminService.updateSkillReportStatus(reportId, ReportStatus.APPROVED).subscribe({
      next: () => {
        this.deleteReason = '';
        this.loadReports();
        this.toastr.success('Skill deleted successfully');
      },
      error: (error) => {
        console.error('Error deleting skill:', error);
        this.toastr.error('Error deleting skill');
      }
    });
  }

  handleRejectReport(reportId: number) {
    this.adminService.updateSkillReportStatus(reportId, ReportStatus.REJECTED).subscribe({
      next: () => {
        this.loadReports();
        this.toastr.success('Report rejected successfully');
      },
      error: (error) => {
        console.error('Error rejecting report:', error);
        this.toastr.error('Error rejecting report');
      }
    });
  }
}
