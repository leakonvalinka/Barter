package at.ac.ase.inso.group02.authentication;

import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.UserState;
import at.ac.ase.inso.group02.entities.auth.VerificationToken;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

@QuarkusTest
public class UserRepositoryTest {

    @Inject
    UserRepository userRepository;

    @Inject
    TransactionManager transactionManager;

    @Test
    @Transactional
    public void testCreateNewUser_shouldSucceedAndSetID() {
        // Arrange
        String email = "some@email.com";
        VerificationToken token = VerificationToken
                .builder()
                .code("131269")
                .expiration(Instant.now())
                .build();

        User newUser = User.builder()
                .email(email)
                .password("TesT_ASE24W")
                .username("someUsername")
                .verificationToken(token)
                .build();

        Assertions.assertNull(newUser.getId());

        // Act
        userRepository.persistUser(newUser);

        // Assert
        Assertions.assertNotNull(newUser.getId());
        Assertions.assertNotNull(newUser.getCreatedAt());
        Assertions.assertEquals(email, userRepository.findByEmail(email).getEmail());
    }


    @Test
    @Transactional
    public void testModifyUser_shouldSucceed() {
        // Arrange
        String myUsername = "myUsername";
        VerificationToken token = VerificationToken
                .builder()
                .code("131269")
                .expiration(Instant.now())
                .build();
        User newUser = User.builder()
                .email("my@email.com")
                .password("TesT_ASE24W")
                .username(myUsername)
                .verificationToken(token)
                .build();

        // Act
        userRepository.persistUser(newUser);

        String myName = "My Name";
        newUser.setDisplayName(myName);
        newUser.setVerificationToken(token.toBuilder().code("691312").build());

        // Assert
        // fetch the user from the DB, modifications should automatically be persisted
        Assertions.assertEquals(myName, userRepository.findByUsername(myUsername).getDisplayName());
    }

    @Test
    @Transactional
    public void testDeleteUser_shouldSucceed() {
        // Arrange
        User newUser = User.builder()
                .email("todelete@email.com")
                .password("TesT_ASE24W")
                .username("myDeletionUsername")
                .build();

        // Act & Assert
        userRepository.persistUser(newUser);
        Assertions.assertNotNull(newUser.getId());
        userRepository.deleteUser(newUser);
        // fetch the user from the DB, modifications should automatically be persisted
        Assertions.assertNull(userRepository.findByEmail("todelete@email.com"));
    }

    @Test
    @Transactional
    public void testGetUserByNonExistentEmail_shouldFail() {
        Assertions.assertNull(userRepository.findByEmail("nonexistent@email.com"));
    }

    @Test
    public void testCreateUserDuplicateEmail_shouldFail() throws NotSupportedException, SystemException {
        // Arrange
        // manually start and commit transactions here
        transactionManager.begin();

        User newUser = User.builder()
                .email("duplicate@email.com")
                .password("TesT_ASE24W")
                .username("myDuplicateUsername")
                .build();

        // Act
        userRepository.persistUser(newUser);

        // create a second user with the same email
        User newUser2 = User.builder()
                .email("duplicate@email.com")
                .password("TesT_ASE24W")
                .username("myDuplicateUsername2")
                .build();

        // Act & Assert
        // don't care about the exception, it just should throw one
        Assertions.assertThrows(Exception.class, () -> {
            userRepository.persistUser(newUser2);
            transactionManager.commit();
        });
    }

    @Test
    @Transactional
    public void testGetNewUserState_shouldReturnNeedsConfirmation() {
        // Arrange
        User newUser = User.builder()
                .email("my-user@email.com")
                .password("TesT_ASE24W")
                .username("myUser")
                .build();

        // Act
        userRepository.persistUser(newUser);
        // Assert
        Assertions.assertEquals(UserState.NEEDS_EMAIL_CONFIRM, newUser.getState());
    }
}
