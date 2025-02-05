package at.ac.ase.inso.group02.admin.dtos;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for User Report information
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserReportDTO {
    private Long id;
    private String reportedUserUsername;
    private String reportingUserUsername;
    private String reason;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;
}