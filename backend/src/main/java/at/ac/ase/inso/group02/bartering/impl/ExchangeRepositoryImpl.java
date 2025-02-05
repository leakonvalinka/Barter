package at.ac.ase.inso.group02.bartering.impl;

import at.ac.ase.inso.group02.bartering.ExchangeRepository;
import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.exchange.ExchangeChat;
import at.ac.ase.inso.group02.entities.exchange.ExchangeItem;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class ExchangeRepositoryImpl implements ExchangeRepository {
    @Override
    public Optional<ExchangeItem> findExistingExchangeItem(ExchangeChat exchangeChat, Skill exchangedSkill, User initiator) {
        return exchangeChat == null ? Optional.empty() :
                find("exchangeChat = :exchangeChat AND exchangedSkill = :exchangedSkill AND initiator = :initiator",
                        Parameters.with("exchangeChat", exchangeChat).and("exchangedSkill", exchangedSkill).and("initiator", initiator)).firstResultOptional();

    }
}
