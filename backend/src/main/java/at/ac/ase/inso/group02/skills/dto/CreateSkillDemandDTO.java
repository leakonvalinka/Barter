package at.ac.ase.inso.group02.skills.dto;

import at.ac.ase.inso.group02.entities.DemandUrgency;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("SkillDemand")
public class CreateSkillDemandDTO extends CreateSkillDTO {

    @NotNull
    private DemandUrgency urgency;
}
