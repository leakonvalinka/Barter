package at.ac.ase.inso.group02.admin;

import java.util.List;

import at.ac.ase.inso.group02.entities.admin.ReportStatus;
import at.ac.ase.inso.group02.entities.admin.SkillReport;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

/**
 * Repository interface for managing Skill Reports in the database.
 * Extends PanacheRepository for basic CRUD operations.
 */
public interface SkillReportRepository extends PanacheRepository<SkillReport> {
    /**
     * Finds all reports for a specific skill.
     *
     * @param skillId The ID of the reported skill
     * @return List of reports for the skill
     */
    List<SkillReport> findBySkillId(Long skillId);

    /**
     * Finds all reports with a specific status.
     *
     * @param status The status to filter by
     * @return List of reports with the specified status
     */
    List<SkillReport> findByStatus(ReportStatus status);

    /**
     * Finds all pending reports for a specific skill.
     *
     * @param skillId The ID of the skill
     * @return List of pending reports for the skill
     */
    List<SkillReport> findPendingBySkillId(Long skillId);
}