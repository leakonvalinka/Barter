package at.ac.ase.inso.group02.admin;

import at.ac.ase.inso.group02.authentication.UserRepository;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.admin.UserBan;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

@QuarkusTest
@TestTransaction
public class UserBanRepositoryTest {

    @Inject
    UserBanRepository userBanRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    TransactionManager transactionManager;

    @Test
    @Transactional
    public void testCheckUserBan_shouldReturnTrue() {
        // Arrange
        // Create and persist test user first
        User testUser = User.builder()
                .email("banned@email.com")
                .password("TesT_ASE24W")
                .username("bannedUser")
                .build();
        userRepository.persistUser(testUser);
        Assertions.assertNotNull(testUser.getId());

        // Create and persist user ban
        UserBan userBan = UserBan.builder()
                .user(testUser)
                .reason("Test ban reason")
                .bannedAt(LocalDateTime.now())
                .build();

        userBanRepository.persist(userBan);
        Assertions.assertNotNull(userBan.getId());

        // Act
        boolean isBanned = userBanRepository.isUserBanned(testUser.getId());
        //Assert
        Assertions.assertTrue(isBanned);
    }

    @Test
    @Transactional
    public void testCheckUserBan_nonExistentUser_shouldReturnFalse() {
        // Act
        boolean isBanned = userBanRepository.isUserBanned(999L);
        // Assert
        Assertions.assertFalse(isBanned);
    }

    @Test
    @Transactional
    public void testCheckUserBan_nullUserId_shouldReturnFalse() {
        // Act
        boolean isBanned = userBanRepository.isUserBanned(null);
        // Assert
        Assertions.assertFalse(isBanned);
    }

    @Test
    @Transactional
    public void testMultipleBansForUser_shouldReturnTrue() {
        // Arrange
        // Create and persist test user first
        User testUser = User.builder()
                .email("multiplebans@email.com")
                .password("TesT_ASE24W")
                .username("multipleBannedUser")
                .build();
        userRepository.persistUser(testUser);
        Assertions.assertNotNull(testUser.getId());

        // Create and persist first ban
        UserBan firstBan = UserBan.builder()
                .user(testUser)
                .reason("First ban reason")
                .bannedAt(LocalDateTime.now())
                .build();

        // Create and persist second ban
        UserBan secondBan = UserBan.builder()
                .user(testUser)
                .reason("Second ban reason")
                .bannedAt(LocalDateTime.now().plusDays(1))
                .build();

        userBanRepository.persist(firstBan);
        userBanRepository.persist(secondBan);

        // Act
        boolean isBanned = userBanRepository.isUserBanned(testUser.getId());
        // Assert
        Assertions.assertTrue(isBanned);
    }

    @Test
    @Transactional
    public void testPersistUserBan_shouldSucceedAndSetId() {
        // Arrange
        // Create and persist test user first
        User testUser = User.builder()
                .email("tobebanned@email.com")
                .password("TesT_ASE24W")
                .username("toBeBannedUser")
                .build();
        userRepository.persistUser(testUser);
        Assertions.assertNotNull(testUser.getId());

        UserBan userBan = UserBan.builder()
                .user(testUser)
                .reason("Test ban reason")
                .bannedAt(LocalDateTime.now())
                .build();

        Assertions.assertNull(userBan.getId());
        userBanRepository.persist(userBan);
        Assertions.assertNotNull(userBan.getId());
        Assertions.assertNotNull(userBan.getBannedAt());
    }

    @Test
    @Transactional
    public void testDeleteUserBan_shouldSucceed() {
        // Arrange
        // Create and persist test user first
        User testUser = User.builder()
                .email("unban@email.com")
                .password("TesT_ASE24W")
                .username("unbanUser")
                .build();
        userRepository.persistUser(testUser);
        Assertions.assertNotNull(testUser.getId());

        UserBan userBan = UserBan.builder()
                .user(testUser)
                .reason("Test ban reason")
                .bannedAt(LocalDateTime.now())
                .build();

        userBanRepository.persist(userBan);
        Assertions.assertNotNull(userBan.getId());

        // Act
        userBanRepository.delete(userBan);

        // Assert
        boolean isBanned = userBanRepository.isUserBanned(testUser.getId());
        Assertions.assertFalse(isBanned);
    }

    @Test
    @Transactional
    public void testFindByUserId_existingBan_shouldReturnBan() {
        // Arrange
        // Create and persist test user
        User testUser = User.builder()
                .email("findban@email.com")
                .password("TesT_ASE24W")
                .username("findBanUser")
                .build();
        userRepository.persistUser(testUser);
        Assertions.assertNotNull(testUser.getId());

        // Create and persist user ban
        UserBan expectedBan = UserBan.builder()
                .user(testUser)
                .reason("Test ban reason")
                .bannedAt(LocalDateTime.now())
                .build();
        userBanRepository.persist(expectedBan);

        // Act
        // Find the ban by user ID
        UserBan foundBan = userBanRepository.findByUserId(testUser.getId());

        // Assert
        // Verify the found ban matches the expected ban
        Assertions.assertNotNull(foundBan);
        Assertions.assertEquals(expectedBan.getId(), foundBan.getId());
        Assertions.assertEquals(expectedBan.getReason(), foundBan.getReason());
        Assertions.assertEquals(expectedBan.getUser().getId(), foundBan.getUser().getId());
    }

    @Test
    @Transactional
    public void testFindByUserId_nonExistentUser_shouldReturnNull() {
        // Act
        UserBan foundBan = userBanRepository.findByUserId(999L);
        // Assert
        Assertions.assertNull(foundBan);
    }

    @Test
    @Transactional
    public void testFindByUserId_nullUserId_shouldReturnNull() {
        // Act
        UserBan foundBan = userBanRepository.findByUserId(null);
        // Assert
        Assertions.assertNull(foundBan);
    }

    @Test
    @Transactional
    public void testFindByUserId_multipleActiveBans_shouldReturnFirstBan() {
        // Arrange
        // Create and persist test user
        User testUser = User.builder()
                .email("multiplebansfind@email.com")
                .password("TesT_ASE24W")
                .username("multipleBansFindUser")
                .build();
        userRepository.persistUser(testUser);
        Assertions.assertNotNull(testUser.getId());

        // Create and persist first ban
        UserBan firstBan = UserBan.builder()
                .user(testUser)
                .reason("First ban reason")
                .bannedAt(LocalDateTime.now())
                .build();
        userBanRepository.persist(firstBan);

        // Create and persist second ban
        UserBan secondBan = UserBan.builder()
                .user(testUser)
                .reason("Second ban reason")
                .bannedAt(LocalDateTime.now().plusDays(1))
                .build();
        userBanRepository.persist(secondBan);

        // Act
        // Find ban by user ID
        UserBan foundBan = userBanRepository.findByUserId(testUser.getId());

        // Assert
        // Verify we got the first ban
        Assertions.assertNotNull(foundBan);
        Assertions.assertEquals(firstBan.getId(), foundBan.getId());
        Assertions.assertEquals("First ban reason", foundBan.getReason());
    }
}