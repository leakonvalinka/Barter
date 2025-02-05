package at.ac.ase.inso.group02.authentication.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserLoginDTO {
    @NotBlank(message = "Email or username is required")
    private String emailOrUsername;

    @NotBlank(message = "Password is required")
    private String password;
}
