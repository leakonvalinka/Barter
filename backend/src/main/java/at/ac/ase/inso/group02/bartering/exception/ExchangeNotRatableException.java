package at.ac.ase.inso.group02.bartering.exception;

/**
 * thrown when a user tries to create a rating for an exchange he would have access to but the exchange cannot yet accept ratings
 */
public class ExchangeNotRatableException extends RuntimeException {
    public ExchangeNotRatableException(String message) {
        super(message);
    }

    public ExchangeNotRatableException(String message, Throwable cause) {
        super(message, cause);
    }
}
