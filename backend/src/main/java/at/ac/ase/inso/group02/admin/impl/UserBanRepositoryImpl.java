package at.ac.ase.inso.group02.admin.impl;

import at.ac.ase.inso.group02.admin.UserBanRepository;
import at.ac.ase.inso.group02.entities.admin.UserBan;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserBanRepositoryImpl implements UserBanRepository {
    @Override
    public boolean isUserBanned(Long userId) {
        return find("user.id", userId).firstResult() != null;
    }

    @Override
    public UserBan findByUserId(Long userId) {
        return find("user.id", userId).firstResult();
    }
}