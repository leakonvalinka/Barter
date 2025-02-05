package at.ac.ase.inso.group02.authentication.impl;

import at.ac.ase.inso.group02.authentication.RefreshTokenRepository;
import at.ac.ase.inso.group02.entities.auth.RefreshToken;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

/**
 * implementation for TokenRepository using PanacheRepository
 */
@ApplicationScoped
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository, PanacheRepository<RefreshToken> {
    @Override
    public RefreshToken findByTokenUUID(UUID tokenUUID) {
        return find("uuid", tokenUUID).firstResult();
    }

    @Override
    public boolean saveNew(RefreshToken refreshToken) {
        if (findByTokenUUID(refreshToken.getUuid()) == null) {
            persist(refreshToken);
            flush();
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(RefreshToken refreshToken) {
        if (isPersistent(refreshToken)) {
            delete(refreshToken);
            flush();
            return true;
        }
        return false;
    }
}
