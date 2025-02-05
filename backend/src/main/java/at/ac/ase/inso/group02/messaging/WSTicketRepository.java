package at.ac.ase.inso.group02.messaging;

import at.ac.ase.inso.group02.entities.messaging.WSTicket;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import java.util.UUID;

/**
 * Repository for one-time-use authentication tickets for new WebSocket connections
 */
public interface WSTicketRepository extends PanacheRepositoryBase<WSTicket, UUID> {
}
