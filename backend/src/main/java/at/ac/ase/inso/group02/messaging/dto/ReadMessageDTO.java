package at.ac.ase.inso.group02.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;
import org.hibernate.validator.constraints.UUID;

/**
 * data that is sent by the client to explicitly tell the server that they read a specific message
 */
@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadMessageDTO {
    @NonNull
    @NotBlank
    private String chatMessageID;
}
