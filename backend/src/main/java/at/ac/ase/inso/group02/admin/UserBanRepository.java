package at.ac.ase.inso.group02.admin;

import at.ac.ase.inso.group02.entities.admin.UserBan;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

/**
 * Repository interface for managing User Bans in the database.
 * Extends PanacheRepository for basic CRUD operations.
 */
public interface UserBanRepository extends PanacheRepository<UserBan> {
    /**
     * Checks if a user is currently banned.
     *
     * @param userId The ID of the user to check
     * @return true if the user is banned, false otherwise
     */
    boolean isUserBanned(Long userId);

    /**
     * Tries to get the UserBan entitiy of a banned user.
     *
     * @param userId The ID of the user to check
     * @return UserBan if the user is banned, null otherwise
     */
    UserBan findByUserId(Long userId);
}
