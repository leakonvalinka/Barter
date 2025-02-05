package at.ac.ase.inso.group02.bartering.inactivity;

import java.io.IOException;
import java.util.UUID;

public interface ExchangeInactivityService {
    void declareChatQueue(UUID exchangeUUID) throws IOException;

    void onNewMessageForExchangeChat(UUID exchangeUUID) throws IOException;
}
