package at.ac.ase.inso.group02.authentication;

import at.ac.ase.inso.group02.entities.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

/**
 * Handles Database operations for the User entity
 */
public interface UserRepository extends PanacheRepository<User> {
    /**
     * Finds a single user by their unique email
     *
     * @param email the unique email of the user
     * @return the User object with that email, or null if there is none
     */
    User findByEmail(String email);

    /**
     * Finds a single user by their unique username
     *
     * @param username the unique username of the user
     * @return the User object with that username, or null if there is none
     */
    User findByUsername(String username);

    /**
     * Finds a single user by their unique id
     *
     * @param id the id of the user
     * @return the User object with that id, or null if there is none
     */
    User findById(Long id);

    /**
     * Persists a user in the database.
     *
     * @param user the user to save
     * @return the saved user, possibly with updated values (e.g. auto-generated id)
     */
    User persistUser(User user);

    /**
     * Deletes a user from the database
     *
     * @param user the user to delete
     * @return true if changes to the db were made
     */
    boolean deleteUser(User user);
}
