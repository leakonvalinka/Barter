package at.ac.ase.inso.group02.authentication;

import at.ac.ase.inso.group02.authentication.dto.UserDetailDTO;
import at.ac.ase.inso.group02.authentication.dto.UserUpdateDTO;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.exceptions.UnauthenticatedException;

/**
 * Service for User management
 */
public interface UserService {
    /**
     * Gets User by their username
     *
     * @param username - the username of the User
     * @return details about the user
     */
    UserDetailDTO getUserByUsername(String username);

    /**
     * Gets a User entity by their username (only for use inside other services)
     * @param username - the username of the user
     * @return the User entity representing the user with the given Username
     */
    User getUserEntityByUsername(String username);

    /**
     * Update a user's profile information.
     *
     * @param updateData - the new data to update the user with
     * @return null
     */
    UserDetailDTO updateUser(UserUpdateDTO updateData);

    /**
     * Delete the current user permanently from the database.
     */
    void deleteUser();

    /**
     * @return data about the currently logged in user
     * @throws UnauthenticatedException if the user is improperly authenticated
     *                                  or their user entry no longer exists in the database
     */
    UserDetailDTO getCurrentUser();
}
