package at.ac.ase.inso.group02.messaging.exceptions;

/**
 * thrown when a WebSocket connection is initiated with an illegal ticket
 */
public class IllegalWSTicketException extends RuntimeException {
    public IllegalWSTicketException(String message) {
        super(message);
    }

    public IllegalWSTicketException(String message, Throwable cause) {
        super(message, cause);
    }
}
