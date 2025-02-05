package at.ac.ase.inso.group02.bartering;

import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.exchange.ExchangeChat;
import at.ac.ase.inso.group02.entities.exchange.ExchangeItem;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import java.util.Optional;

/**
 * Repository for ExchangeItems
 */
public interface ExchangeRepository extends PanacheRepository<ExchangeItem> {
    /**
     * optionally finds an ExchangeItem that matches all parameters
     * @param exchangeChat the exchangeChat in which the ExchangeItem appears in
     * @param exchangedSkill the exchanged Skill for this ExchangeItem
     * @param initiator the initiator (=counterpart-user) for this ExchangeItem
     * @return an optional ExchangeItem fitting all the parameters, if one exists
     */
    Optional<ExchangeItem> findExistingExchangeItem(ExchangeChat exchangeChat, Skill exchangedSkill, User initiator);
}
