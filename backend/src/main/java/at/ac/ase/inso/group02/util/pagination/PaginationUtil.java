package at.ac.ase.inso.group02.util.pagination;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Utility for paginating a PanacheQuery
 */
public class PaginationUtil {

    /**
     * paginate the given PanacheQuery (lazily)
     *
     * @param params          pagination-parameters (page-index, page-size)
     * @param query           the PanacheQuery to paginate
     * @param mappingFunction A function mapping entities from the query to dtos
     * @param <E>             entity type
     * @param <D>             return DTO type (type of PaginatedQueryDTO.items)
     * @return a PaginatedQueryDTO with items containing the data of the entities in the query at the respective page
     */
    public static <E, D> PaginatedQueryDTO<D> getPaginatedQueryDTO(PaginationParamsDTO params, PanacheQuery<E> query, Function<E, D> mappingFunction) {
        query = query.page(Page.of(params.getPage(), params.getPageSize()));

        List<D> items = query
                .stream()
                .map(mappingFunction)
                .toList();

        Page currentPage = query.page();

        return PaginatedQueryDTO.<D>builder()
                .total(query.count())
                .page(currentPage.index)
                .pageSize(currentPage.size)
                .hasMore(currentPage.index < query.pageCount() - 1)
                .items(items)
                .build();
    }
}
