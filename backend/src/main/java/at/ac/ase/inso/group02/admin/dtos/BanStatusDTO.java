package at.ac.ase.inso.group02.admin.dtos;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for ban status response
 */
@Data
@Builder
public class BanStatusDTO {
    private boolean banned;
}