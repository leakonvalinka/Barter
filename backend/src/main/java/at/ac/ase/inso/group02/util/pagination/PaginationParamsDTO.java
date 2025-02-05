package at.ac.ase.inso.group02.util.pagination;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.jboss.resteasy.reactive.RestQuery;

@Data
public class PaginationParamsDTO {

    @RestQuery
    @Min(0)
    private int page = 0;

    @RestQuery
    private int pageSize = 50;
}
