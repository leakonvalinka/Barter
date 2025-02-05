package at.ac.ase.inso.group02.admin.dtos;

import java.time.LocalDateTime;

import at.ac.ase.inso.group02.entities.admin.ReportStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Skill Report information
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SkillReportDTO {
    private Long id;
    private Long skillId;
    private String reportingUserUsername;
    private String reason;
    private ReportStatus status;
    private String skillType;  // "DEMAND" or "OFFER"
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime resolvedAt;
}