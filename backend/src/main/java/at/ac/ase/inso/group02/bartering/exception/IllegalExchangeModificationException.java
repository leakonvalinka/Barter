package at.ac.ase.inso.group02.bartering.exception;

/**
 * thrown when an exchange is updated with new data that conflicts with old data
 * For example, when an Exchange-Item is removed that is already finalized and already has ratings
 */
public class IllegalExchangeModificationException extends RuntimeException {
    public IllegalExchangeModificationException(String message) {
        super(message);
    }

    public IllegalExchangeModificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
