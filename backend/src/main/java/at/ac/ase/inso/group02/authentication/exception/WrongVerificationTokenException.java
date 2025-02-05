package at.ac.ase.inso.group02.authentication.exception;

/**
 * WrongVerificationTokenException
 */
public class WrongVerificationTokenException extends RuntimeException {
    public WrongVerificationTokenException(String message) {
        super(message);
    }

    public WrongVerificationTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
