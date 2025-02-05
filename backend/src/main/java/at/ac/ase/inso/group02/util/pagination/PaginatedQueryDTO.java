package at.ac.ase.inso.group02.util.pagination;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginatedQueryDTO<T> {
    private int page;
    private int pageSize;
    private long total;

    private boolean hasMore;

    private Collection<T> items;
}
