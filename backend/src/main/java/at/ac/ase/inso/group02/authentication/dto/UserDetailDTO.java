package at.ac.ase.inso.group02.authentication.dto;

import at.ac.ase.inso.group02.skills.dto.SkillDemandDTO;
import at.ac.ase.inso.group02.skills.dto.SkillOfferDTO;
import at.ac.ase.inso.group02.views.Views;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.util.Set;


/**
 * DTO for all user details
 */
@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetailDTO {

    @JsonView(Views.Private.class)
    @NotNull(message = "Email cannot be null")
    private String email;

    @NotNull(message = "Username cannot be null")
    private String username;

    private String displayName;

    private String bio;

    /*
    from client to server, this can be a Base64-encoded image
    from server to client, this is an image UUID for retrieval via /images/uuid
     */
    private String profilePicture;

    private UserLocationDTO location;

    @JsonView(Views.Public.class)
    private Set<SkillDemandDTO> skillDemands;

    @JsonView(Views.Public.class)
    private Set<SkillOfferDTO> skillOffers;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    private Double averageRatingHalfStars;

    private Long numberOfRatings;
}
