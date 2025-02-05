package at.ac.ase.inso.group02.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@DiscriminatorValue("DEMAND")
public class SkillDemand extends Skill {
    @Builder.Default
    private DemandUrgency urgency = DemandUrgency.NONE;
}
