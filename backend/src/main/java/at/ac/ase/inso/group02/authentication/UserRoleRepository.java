package at.ac.ase.inso.group02.authentication;

import at.ac.ase.inso.group02.entities.UserRole;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

/**
 * basic repository for the UserRole entity
 */
public interface UserRoleRepository extends PanacheRepository<UserRole> {
}
