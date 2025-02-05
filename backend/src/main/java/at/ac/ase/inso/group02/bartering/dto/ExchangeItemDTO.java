package at.ac.ase.inso.group02.bartering.dto;


import at.ac.ase.inso.group02.authentication.dto.UserDetailDTO;
import at.ac.ase.inso.group02.rating.dto.UserRatingDTO;
import at.ac.ase.inso.group02.skills.dto.SkillDTO;
import at.ac.ase.inso.group02.views.Views;
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
public class ExchangeItemDTO {
    private Long id;
    private SkillDTO exchangedSkill;
    private SkillDTO exchangedSkillCounterpart;

    @JsonView({Views.Brief.class})
    private UserDetailDTO initiator;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime firstExchangeAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime lastExchangeAt;

    private int numberOfExchanges;

    private boolean ratable;

    private boolean initiatorMarkedComplete;

    private UserRatingDTO initiatorRating;

    private boolean responderMarkedComplete;

    private UserRatingDTO responderRating;
}
