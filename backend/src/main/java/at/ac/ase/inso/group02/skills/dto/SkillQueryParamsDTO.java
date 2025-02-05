package at.ac.ase.inso.group02.skills.dto;

import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class SkillQueryParamsDTO extends PaginationParamsDTO {
    @RestQuery
    private Double radius = 5000.0; // default radius = 5000m

    @RestQuery
    private Set<Long> category = Set.of();

    @RestQuery
    private Double lat;

    @RestQuery
    private Double lon;

    @RestQuery
    private boolean includeOwn = false;
}
