package at.ac.ase.inso.group02.skills.dto;

import at.ac.ase.inso.group02.entities.DemandUrgency;
import at.ac.ase.inso.group02.views.Views;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkillDemandDTO extends SkillDTO {
    private DemandUrgency urgency;

    @JsonView({Views.ExplicitlyTypedFull.class, Views.ExplicitlyTypedBrief.class})
    private final String type = "demand";
}
