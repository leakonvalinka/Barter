package at.ac.ase.inso.group02.admin.exception;

public class UserIsBannedException extends RuntimeException {
	public UserIsBannedException(String message) {
        super(message);
    }

    public UserIsBannedException(String message, Throwable cause) {
        super(message, cause);
    }
}
