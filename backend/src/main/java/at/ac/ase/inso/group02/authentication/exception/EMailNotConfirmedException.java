package at.ac.ase.inso.group02.authentication.exception;

/**
 * thrown when a user tries to login, but they did not confirm their email yet
 */
public class EMailNotConfirmedException extends RuntimeException {
    public EMailNotConfirmedException(String message) {
        super(message);
    }

    public EMailNotConfirmedException(String message, Throwable cause) {
        super(message, cause);
    }
}
