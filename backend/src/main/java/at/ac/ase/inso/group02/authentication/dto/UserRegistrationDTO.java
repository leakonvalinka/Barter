package at.ac.ase.inso.group02.authentication.dto;

import at.ac.ase.inso.group02.authentication.validation.NotEmail;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRegistrationDTO {
    @Email
    @NotBlank(message = "Email is required")
    private String email;

    @JsonProperty(required = true)  // Jackson requires this field in the JSON payload
    @Setter(AccessLevel.NONE)       // Lombok prevents setting this field directly on the entity
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one uppercase letter")
    @Pattern(regexp = ".*[a-z].*", message = "Password must contain at least one lowercase letter")
    @Pattern(regexp = ".*\\d.*", message = "Password must contain at least one digit")
    @Pattern(regexp = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*", message = "Password must contain at least one special character")
    private String password;

    @NotBlank
    @NotEmail
    private String username;
}
