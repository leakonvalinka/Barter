package at.ac.ase.inso.group02.authentication.exception;

/**
 * thrown when trying to refresh a JWT token with an invalid refresh token (e.g. because it expired)
 */
public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }

    public InvalidRefreshTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
