package at.ac.ase.inso.group02.admin.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for User Ban information
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserBanDTO {
    private Long id;
    private String username;
    private String reason;
    private LocalDateTime bannedAt;
}