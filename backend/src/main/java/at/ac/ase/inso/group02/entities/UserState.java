package at.ac.ase.inso.group02.entities;

/**
 * Represents the state of users. For example, newly registered users are in state NeedsPasswordConfirm
 */
public enum UserState {
    ACTIVE,
    NEEDS_EMAIL_CONFIRM,
    INACTIVE
}
