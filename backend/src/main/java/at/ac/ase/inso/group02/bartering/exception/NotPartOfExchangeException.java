package at.ac.ase.inso.group02.bartering.exception;

/**
 * thrown when a user queries an exchange they are not part of
 * Note that when trying to modify such an exchange, UnauthorizedModificationException is thrown
 */
public class NotPartOfExchangeException extends RuntimeException {
    public NotPartOfExchangeException(String message) {
        super(message);
    }

  public NotPartOfExchangeException(String message, Throwable cause) {
    super(message, cause);
  }
}
