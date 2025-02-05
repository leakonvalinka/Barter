package at.ac.ase.inso.group02.rating.dto;

import at.ac.ase.inso.group02.authentication.dto.UserDetailDTO;
import at.ac.ase.inso.group02.rating.views.RatingViews;
import at.ac.ase.inso.group02.skills.dto.SkillDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRatingDTO {
    private Long id;

    private int ratingHalfStars;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;
    private String title;
    private String description;

    @JsonView({RatingViews.IncludeByUser.class, RatingViews.IncludeAllUsers.class})
    private UserDetailDTO byUser;

    @JsonView({RatingViews.IncludeForUser.class, RatingViews.IncludeAllUsers.class})
    private UserDetailDTO forUser;

    // NOTE: this skill might not be created by user forUser, if forUser did not exchange anything in return for byUser's skill
    // In that case, it is only byUser's skill

    @JsonView({RatingViews.IncludeForUser.class, RatingViews.IncludeByUser.class, RatingViews.IncludeAllUsers.class})
    private SkillDTO forSkill;
}
