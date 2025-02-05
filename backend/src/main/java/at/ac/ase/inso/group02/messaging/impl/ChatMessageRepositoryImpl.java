package at.ac.ase.inso.group02.messaging.impl;

import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.exchange.ExchangeChat;
import at.ac.ase.inso.group02.entities.messaging.ChatMessage;
import at.ac.ase.inso.group02.messaging.ChatMessageRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ChatMessageRepositoryImpl implements ChatMessageRepository {
    @Override
    public List<ChatMessage> getMessagesForExchange(ExchangeChat exchangeChat, ChatMessage beforeMessage, Long count) {
        Parameters params = Parameters.with("exchangeChat", exchangeChat).and("count", count);

        String queryStr = "SELECT m FROM ChatMessage m WHERE m.exchangeChat = :exchangeChat";

        if(beforeMessage != null) {
            queryStr += " and m.timestamp < :before";
            params = params.and("before", beforeMessage.getTimestamp());
        }

        queryStr += " ORDER BY m.timestamp DESC LIMIT :count";

        return find(queryStr, params).list();
    }

    @Override
    public ChatMessage getMostRecentMessageForExchange(ExchangeChat exchangeChat) {
        return find("SELECT m FROM ChatMessage m WHERE m.exchangeChat = :chat ORDER BY m.timestamp DESC LIMIT 1",
                Parameters.with("chat", exchangeChat)).firstResult();
    }

    @Override
    public PanacheQuery<ChatMessage> getUnreadMessagesForUser(User user) {
        return find(
                "SELECT m FROM ChatMessage m JOIN m.unseenBy u WHERE u.user = :user ORDER BY m.timestamp DESC"
                ,
                Parameters.with("user", user)
        );
    }

    @Override
    public Long getUnreadMessagesCount(ExchangeChat exchangeChat, User user) {
        return count(
                "SELECT count(m) FROM ChatMessage m JOIN m.unseenBy u WHERE u.user = :user AND m.exchangeChat = :exchangeChat",
                Parameters.with("user", user).and("exchangeChat", exchangeChat)
        );
    }
}
