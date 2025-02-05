package at.ac.ase.inso.group02.authentication.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * DTO for all data that the user may update
 */
@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserUpdateDTO {
    private String displayName;

    private String bio;

    /*
    from client to server, this can be a Base64-encoded image
    from server to client, this is an image UUID for retrieval via /images/uuid
     */
    private String profilePicture;

    private UserLocationDTO location;
}
