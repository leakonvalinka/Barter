package at.ac.ase.inso.group02.authentication.dto;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class TokenRefreshDTO {
    private String refreshToken;
}
