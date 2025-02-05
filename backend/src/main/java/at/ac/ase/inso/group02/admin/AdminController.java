package at.ac.ase.inso.group02.admin;

import at.ac.ase.inso.group02.admin.dtos.SkillReportDTO;
import at.ac.ase.inso.group02.admin.dtos.*;
import at.ac.ase.inso.group02.entities.admin.ReportStatus;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

/**
 * REST Controller interface for administrative actions.
 * Provides endpoints for managing bans, user reports, and skill reports.
 */
@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Admin", description = "Administrative endpoints for managing bans and reports")
public interface AdminController {

    /**
     * Bans a user permanently.
     *
     * @param username Username of the user to ban
     * @param reason Reason for the ban
     * @return Details of the created ban
     */
    @POST
    @Path("/ban/{username}")
    @RolesAllowed("ADMIN")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "User successfully banned"),
        @APIResponse(responseCode = "404", description = "User not found"),
        @APIResponse(responseCode = "409", description = "User is already banned"),
        @APIResponse(responseCode = "403", description = "Insufficient permissions")
    })
    UserBanDTO banUser(
        @PathParam("username") String username,
        @QueryParam("reason") String reason
    );

    /**
     * Checks if a user is banned.
     *
     * @param username Username of the user to check
     * @return Ban status of the user
     */
    @GET
    @Path("/ban/{username}")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Ban status retrieved successfully"),
        @APIResponse(responseCode = "404", description = "User not found")
    })
    BanStatusDTO isUserBanned(@PathParam("username") String username);

    /**
     * Reports a user for inappropriate behavior.
     *
     * @param reportedUserUsername Username of the user being reported
     * @param reason Reason for the report
     * @return Details of the created report
     */
    @POST
    @Path("/reports/users/{reportedUserUsername}")
    @RolesAllowed({"USER", "ADMIN"})
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Report created successfully"),
        @APIResponse(responseCode = "404", description = "User not found"),
        @APIResponse(responseCode = "403", description = "Insufficient permissions")
    })
    UserReportDTO reportUser(
        @PathParam("reportedUserUsername") String reportedUserUsername,
        @QueryParam("reason") String reason
    );

    /**
     * Gets all reports for a user.
     *
     * @param username Username of the user to get reports for
     * @return List of reports for the user
     */
    @GET
    @Path("/reports/users/{username}")
    @RolesAllowed("ADMIN")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Reports retrieved successfully"),
        @APIResponse(responseCode = "403", description = "Insufficient permissions")
    })
    List<UserReportDTO> getUserReports(@PathParam("username") String username);

    /**
     * Reports a skill for inappropriate content.
     *
     * @param skillId ID of the skill being reported
     * @param reason Reason for the report
     * @return Details of the created report
     */
    @POST
    @Path("/reports/skills/{skillId}")
    @RolesAllowed({"USER", "ADMIN"})
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Report created successfully"),
        @APIResponse(responseCode = "404", description = "Skill not found"),
        @APIResponse(responseCode = "403", description = "Insufficient permissions")
    })
    SkillReportDTO reportSkill(
        @PathParam("skillId") Long skillId,
        @QueryParam("reason") String reason
    );

    /**
     * Gets all reports for a skill.
     *
     * @param skillId ID of the skill to get reports for
     * @return List of reports for the skill
     */
    @GET
    @Path("/reports/skills/{skillId}")
    @RolesAllowed("ADMIN")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Reports retrieved successfully"),
        @APIResponse(responseCode = "403", description = "Insufficient permissions")
    })
    List<SkillReportDTO> getSkillReports(@PathParam("skillId") Long skillId);

    /**
     * Gets all pending skill reports.
     *
     * @return List of pending skill reports
     */
    @GET
    @Path("/reports/skills/pending")
    @RolesAllowed("ADMIN")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Pending reports retrieved successfully"),
        @APIResponse(responseCode = "403", description = "Insufficient permissions")
    })
    List<SkillReportDTO> getPendingSkillReports();

    /**
     * Updates the status of a skill report.
     *
     * @param reportId ID of the report to update
     * @param newStatus New status to set
     * @return Updated report details
     */
    @PUT
    @Path("/reports/skills/{reportId}/status")
    @RolesAllowed("ADMIN")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Report status updated successfully"),
        @APIResponse(responseCode = "404", description = "Report not found"),
        @APIResponse(responseCode = "403", description = "Insufficient permissions")
    })
    SkillReportDTO updateSkillReportStatus(
        @PathParam("reportId") Long reportId,
        @QueryParam("status") ReportStatus newStatus
    );

    /**
     * Gets all pending user reports.
     *
     * @return List of pending user reports
     */
    @GET
    @Path("/reports/users/pending")
    @RolesAllowed("ADMIN")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Pending user reports retrieved successfully"),
        @APIResponse(responseCode = "403", description = "Insufficient permissions")
    })
    List<UserReportDTO> getPendingUserReports();

    /**
     * Gets all user reports.
     *
     * @return List of all user reports
     */
    @GET
    @Path("/reports/users")
    @RolesAllowed("ADMIN")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "All user reports retrieved successfully"),
        @APIResponse(responseCode = "403", description = "Insufficient permissions")
    })
    List<UserReportDTO> getAllUserReports();

    /**
     * Gets all skill reports.
     *
     * @return List of all skill reports
     */
    @GET
    @Path("/reports/skills")
    @RolesAllowed("ADMIN")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "All skill reports retrieved successfully"),
        @APIResponse(responseCode = "403", description = "Insufficient permissions")
    })
    List<SkillReportDTO> getAllSkillReports();

    /**
     * Deletes a user report.
     *
     * @param reportId ID of the report to delete
     */
    @DELETE
    @Path("/reports/users/{reportId}")
    @RolesAllowed("ADMIN")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Report deleted successfully"),
        @APIResponse(responseCode = "404", description = "Report not found"),
        @APIResponse(responseCode = "403", description = "Insufficient permissions")
    })
    void deleteUserReport(@PathParam("reportId") Long reportId);
}