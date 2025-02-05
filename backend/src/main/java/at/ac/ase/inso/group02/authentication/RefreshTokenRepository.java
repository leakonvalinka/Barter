package at.ac.ase.inso.group02.authentication;

import at.ac.ase.inso.group02.entities.auth.RefreshToken;

import java.util.UUID;

/**
 * handles database operations for JWT (refresh) tokens
 */
public interface RefreshTokenRepository {

    /**
     * retrieves a token from the database by its uuid
     *
     * @param tokenUUID the uuid of the (refresh) token
     * @return the corresponding RefreshToken from the database, null if there is no such token
     */
    RefreshToken findByTokenUUID(UUID tokenUUID);

    /**
     * save a new token to the DB
     *
     * @param refreshToken a new RefreshToken
     * @return true if the operation succeeded, false otherwise (e.g. when a token with that token-String already exists)
     */
    boolean saveNew(RefreshToken refreshToken);

    /**
     * manually removes a refresh token from the DB
     *
     * @param refreshToken token to remove
     * @return true if a token was removed, false otherwise
     */
    boolean remove(RefreshToken refreshToken);

    // TODO: auto-remove expired tokens
}
