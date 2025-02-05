package at.ac.ase.inso.group02.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * WebSocket-Ticket DTO, used for one-time authentication before creating the WebSocket
 */

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class WSTicketDTO {
    private String ticketUUID;
    private String expires;
}
