package at.ac.ase.inso.group02.bartering.impl;

import at.ac.ase.inso.group02.bartering.ExchangeChatRepository;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.exchange.ExchangeChat;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ExchangeChatRepositoryImpl implements ExchangeChatRepository {
    @Override
    public PanacheQuery<ExchangeChat> findByUser(User user) {
        return find("select ec from ExchangeChat ec " +
                        "where initiator = :user " +
                        "or :user in (select e.initiator from ExchangeItem e where e.exchangeChat = ec) " +
                        "or :user in (select e.exchangedSkill.byUser from ExchangeItem e where e.exchangeChat = ec) " +
                        "order by (select max(m.timestamp) from ChatMessage m where m.exchangeChat = ec) desc",
                Parameters.with("user", user));
    }
}
