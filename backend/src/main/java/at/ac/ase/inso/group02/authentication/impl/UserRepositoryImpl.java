package at.ac.ase.inso.group02.authentication.impl;

import at.ac.ase.inso.group02.admin.UserBanRepository;
import at.ac.ase.inso.group02.admin.exception.UserIsBannedException;
import at.ac.ase.inso.group02.authentication.UserRepository;
import at.ac.ase.inso.group02.entities.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * implementation for UserRepository using PanacheRepository
 */
@ApplicationScoped
public class UserRepositoryImpl implements UserRepository {
    @Inject
    UserBanRepository banRepository;

    @Override
    public User findByEmail(String email) {
        User user = find("email", email).firstResult();
        if (user != null && banRepository.isUserBanned(user.getId())) {
            throw new UserIsBannedException("User is banned");
        }
        return user;
    }

    @Override
    public User findByUsername(String username) {
        User user = find("username", username).firstResult();
        if (user != null && banRepository.isUserBanned(user.getId())) {
            throw new UserIsBannedException("User is banned");
        }
        return user;
    }

    @Override
    public User findById(Long id) {
        User user = find("id", id).firstResult();
        if (user != null && banRepository.isUserBanned(user.getId())) {
            throw new UserIsBannedException("User is banned");
        }
        return user;
    }

    @Override
    public User persistUser(User user) {
        persist(user);
        flush(); // needs to flush to set createdAt timestamp
        return user;
    }

    @Override
    public boolean deleteUser(User user) {
        if (isPersistent(user)) {
            delete(user);
            return true;
        }
        return false;
    }
}
