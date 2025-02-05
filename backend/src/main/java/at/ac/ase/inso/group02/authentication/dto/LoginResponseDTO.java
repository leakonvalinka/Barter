package at.ac.ase.inso.group02.authentication.dto;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class LoginResponseDTO {
    private String jwt;
    private String refreshToken;
    private Boolean firstLogin;
}
