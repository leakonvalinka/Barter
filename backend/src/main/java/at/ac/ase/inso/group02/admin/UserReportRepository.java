package at.ac.ase.inso.group02.admin;

import java.util.List;

import at.ac.ase.inso.group02.entities.admin.UserReport;
import at.ac.ase.inso.group02.entities.admin.ReportStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

/**
 * Repository interface for managing User Reports in the database.
 * Extends PanacheRepository for basic CRUD operations.
 */
public interface UserReportRepository extends PanacheRepository<UserReport> {
    /**
     * Finds all reports submitted against a specific user.
     *
     * @param userId The ID of the reported user
     * @return List of reports filed against the user
     */
    List<UserReport> findByReportedUserId(Long userId);

    /**
     * Finds all reports submitted by a specific user.
     *
     * @param userId The ID of the reporting user
     * @return List of reports created by the user
     */
    List<UserReport> findByReportingUserId(Long userId);

    /**
     * Finds all reports with a specific status.
     *
     * @param status The status to filter by
     * @return List of reports with the specified status
     */
    List<UserReport> findByStatus(ReportStatus status);
}
