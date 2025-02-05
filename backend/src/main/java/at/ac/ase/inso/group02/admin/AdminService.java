package at.ac.ase.inso.group02.admin;

import java.util.List;

import at.ac.ase.inso.group02.admin.dtos.BanStatusDTO;
import at.ac.ase.inso.group02.admin.dtos.SkillReportDTO;
import at.ac.ase.inso.group02.admin.dtos.UserBanDTO;
import at.ac.ase.inso.group02.admin.dtos.UserReportDTO;
import at.ac.ase.inso.group02.entities.admin.ReportStatus;
import jakarta.ws.rs.NotFoundException;

/**
 * Service interface for handling administrative actions such as bans and reports.
 * This service provides methods for managing user bans, user reports, and skill reports.
 */
public interface AdminService {
    /**
     * Permanently bans a user from the platform.
     *
     * @param username The Username of the user to ban
     * @param reason The reason for the ban
     * @return The created UserBanDTO
     * @throws NotFoundException if the user doesn't exist
     * @throws IllegalStateException if the user is already banned
     */
    UserBanDTO banUser(String username, String reason);

    /**
     * Creates a new report against a user.
     *
     * @param reportedUserUsername The Username of the user being reported
     * @param reason The reason for the report
     * @return The created UserReportDTO
     * @throws NotFoundException if either user doesn't exist
     */
    UserReportDTO reportUser(String reportedUserUsername, String reason);

    /**
     * Creates a new report against a skill.
     *
     * @param skillId The ID of the skill being reported
     * @param reason The reason for the report
     * @return The created SkillReportDTO
     * @throws NotFoundException if the skill or user doesn't exist
     */
    SkillReportDTO reportSkill(Long skillId, String reason);

    /**
     * Updates the status of a skill report.
     *
     * @param reportId The ID of the report to update
     * @param newStatus The new status to set
     * @return The updated SkillReportDTO
     * @throws NotFoundException if the report doesn't exist
     */
    SkillReportDTO updateSkillReportStatus(Long reportId, ReportStatus newStatus);

    /**
     * Checks if a user is currently banned.
     *
     * @param username The Username of the user to check
     * @return true if the user is banned, false otherwise
     */
    BanStatusDTO isUserBanned(String username);

    /**
     * Retrieves all reports filed against a specific user.
     *
     * @param username The Username of the user
     * @return List of reports for the user
     */
    List<UserReportDTO> getUserReports(String username);

    /**
     * Retrieves all reports filed against a specific skill.
     *
     * @param skillId The ID of the skill
     * @return List of reports for the skill
     */
    List<SkillReportDTO> getSkillReports(Long skillId);

    /**
     * Retrieves all pending skill reports.
     *
     * @return List of pending skill reports
     */
    List<SkillReportDTO> getPendingSkillReports();

    /**
     * Retrieves all pending user reports.
     *
     * @return List of pending user reports
     */
    List<UserReportDTO> getPendingUserReports();

    /**
     * Retrieves all user reports.
     *
     * @return List of all user reports
     */
    List<UserReportDTO> getAllUserReports();

    /**
     * Retrieves all skill reports.
     *
     * @return List of all skill reports
     */
    List<SkillReportDTO> getAllSkillReports();

    /**
     * Deletes a user report.
     *
     * @param reportId The ID of the report to delete
     * @throws NotFoundException if the report doesn't exist
     */
    void deleteUserReport(Long reportId);
}
