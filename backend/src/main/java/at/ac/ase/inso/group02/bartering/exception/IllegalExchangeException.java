package at.ac.ase.inso.group02.bartering.exception;

/**
 * thrown when an exchange cannot be created/initiated, for example, because a skill-counterpart was given
 * that was not created by the currently authenticated user
 */
public class IllegalExchangeException extends RuntimeException {
    public IllegalExchangeException(String message) {
        super(message);
    }

    public IllegalExchangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
