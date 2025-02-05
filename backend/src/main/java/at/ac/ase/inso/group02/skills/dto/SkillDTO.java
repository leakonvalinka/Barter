package at.ac.ase.inso.group02.skills.dto;

import at.ac.ase.inso.group02.authentication.dto.UserDetailDTO;
import at.ac.ase.inso.group02.rating.views.RatingViews;
import at.ac.ase.inso.group02.views.Views;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.DEDUCTION;


@Data
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SkillDemandDTO.class, name = "SkillDemand"),
        @JsonSubTypes.Type(value = SkillOfferDTO.class, name = "SkillOffer")
})
public abstract class SkillDTO {
    private Long id;
    private String title;
    private String description;
    private SkillCategoryDTO category;

    @JsonView({Views.Brief.class})
    private UserDetailDTO byUser;

    @JsonView({Views.ExplicitlyTypedFull.class, Views.ExplicitlyTypedBrief.class, Views.ExplicitlyTypedWithoutUser.class, RatingViews.IncludeByUser.class, RatingViews.IncludeForUser.class, RatingViews.IncludeAllUsers.class})
    private final String type = null;

    private Double averageRatingHalfStars;

    private Long numberOfRatings;
}
