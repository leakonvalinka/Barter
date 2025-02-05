package at.ac.ase.inso.group02.authentication.exception;

/**
 * VerificationTokenExpiredException
 */
public class VerificationTokenExpiredException extends RuntimeException {
    public VerificationTokenExpiredException(String message) {
        super(message);
    }

    public VerificationTokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
