package at.ac.ase.inso.group02.skills.exception;

/**
 * represents an error thrown when a referenced Skill does not exist in the database
 */
public class SkillDoesNotExistException extends RuntimeException {
    public SkillDoesNotExistException(String message) {
        super(message);
    }

    public SkillDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
