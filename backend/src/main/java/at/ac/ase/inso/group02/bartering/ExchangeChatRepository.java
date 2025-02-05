package at.ac.ase.inso.group02.bartering;

import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.exchange.ExchangeChat;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import java.util.UUID;

/**
 * repository for ExchangeChat entities
 */
public interface ExchangeChatRepository extends PanacheRepositoryBase<ExchangeChat, UUID> {

    /**
     * retrieves ExchangeChats for a given User
     * @param user user to find ExchangeChats for
     * @return a paginatable PanacheQuery of ExchangeChats where the given User is involved
     */
    PanacheQuery<ExchangeChat> findByUser(User user);
}
