package at.ac.ase.inso.group02.admin.impl;

import at.ac.ase.inso.group02.admin.AdminController;
import at.ac.ase.inso.group02.admin.AdminService;
import at.ac.ase.inso.group02.admin.dtos.*;
import at.ac.ase.inso.group02.entities.admin.ReportStatus;
import io.quarkus.logging.Log;
import lombok.AllArgsConstructor;
import java.util.List;

@AllArgsConstructor
public class AdminControllerImpl implements AdminController {
    
    private AdminService adminService;

    @Override
    public UserBanDTO banUser(String username, String reason) {
        Log.infov("Banning user: {0}", username);
        return adminService.banUser(username, reason);
    }

    @Override
    public BanStatusDTO isUserBanned(String username) {
        Log.infov("Fetching ban status for user: {0}", username);
        return adminService.isUserBanned(username);
    }

    @Override
    public UserReportDTO reportUser(String reportedUserUsername, String reason) {
        Log.infov("Reporting user {0} with reason: {1}", reportedUserUsername, reason);
        return adminService.reportUser(reportedUserUsername, reason);
    }

    @Override
    public List<UserReportDTO> getUserReports(String username) {
        Log.infov("Fetching user reports for user: {0}", username);
        return adminService.getUserReports(username);
    }

    @Override
    public SkillReportDTO reportSkill(Long skillId, String reason) {
        Log.infov("Reporting skill {0} with reason: {1}", skillId, reason);
        return adminService.reportSkill(skillId, reason);
    }

    @Override
    public List<SkillReportDTO> getSkillReports(Long skillId) {
        Log.infov("Fetching skill reports for skill: {0}", skillId);
        return adminService.getSkillReports(skillId);
    }

    @Override
    public List<SkillReportDTO> getPendingSkillReports() {
        Log.infov("Fetching pending skill reports");
        return adminService.getPendingSkillReports();
    }

    @Override
    public List<UserReportDTO> getPendingUserReports() {
        return adminService.getPendingUserReports();
    }

    @Override
    public List<UserReportDTO> getAllUserReports() {
        return adminService.getAllUserReports();
    }

    @Override
    public List<SkillReportDTO> getAllSkillReports() {
        return adminService.getAllSkillReports();
    }

    @Override
    public SkillReportDTO updateSkillReportStatus(Long reportId, ReportStatus newStatus) {
        Log.infov("Updating skill report with id {0} to status {1}", reportId, newStatus);
        return adminService.updateSkillReportStatus(reportId, newStatus);
    }

    @Override
    public void deleteUserReport(Long reportId) {
        adminService.deleteUserReport(reportId);
    }
}