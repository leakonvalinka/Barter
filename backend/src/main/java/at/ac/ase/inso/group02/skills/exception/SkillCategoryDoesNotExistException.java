package at.ac.ase.inso.group02.skills.exception;

/**
 * represents an error thrown when a referenced SkillCategory does not exist in the database
 */
public class SkillCategoryDoesNotExistException extends RuntimeException {
    public SkillCategoryDoesNotExistException(String message) {
        super(message);
    }

    public SkillCategoryDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
