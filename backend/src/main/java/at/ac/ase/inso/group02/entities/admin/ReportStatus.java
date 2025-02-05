package at.ac.ase.inso.group02.entities.admin;

import lombok.*;

/**
 * Represents the possible states of a report in the system.
 * This enum is used to track the status of reports submitted against skill postings.
 */
@Getter
public enum ReportStatus {
/**
     * Initial status when a report is submitted but not yet reviewed by an administrator.
     */
    PENDING,

    /**
     * Status indicating the report has been reviewed and approved by an administrator.
     * When a report is approved, the corresponding skill posting will be automatically deleted from the system.
     */
    APPROVED,

    /**
     * Status indicating the report has been reviewed and rejected by an administrator.
     * The corresponding skill posting remains active in the system.
     */
    REJECTED
}