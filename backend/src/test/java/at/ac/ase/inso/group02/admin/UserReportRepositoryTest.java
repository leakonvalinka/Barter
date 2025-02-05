package at.ac.ase.inso.group02.admin;

import at.ac.ase.inso.group02.authentication.UserRepository;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.admin.UserReport;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

@QuarkusTest
@TestTransaction
public class UserReportRepositoryTest {

    @Inject
    UserReportRepository userReportRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    TransactionManager transactionManager;

    @Test
    @Transactional
    public void testPersistUserReport_shouldSucceedAndSetId() {
        // Arrange
        // Create and persist users first
        User reportedUser = User.builder()
                .email("reported@email.com")
                .password("TesT_ASE24W")
                .username("reportedUser")
                .build();
        userRepository.persistUser(reportedUser);

        User reportingUser = User.builder()
                .email("reporting@email.com")
                .password("TesT_ASE24W")
                .username("reportingUser")
                .build();
        userRepository.persistUser(reportingUser);

        UserReport report = UserReport.builder()
                .reportedUser(reportedUser)
                .reportingUser(reportingUser)
                .reason("Test report reason")
                .createdAt(LocalDateTime.now())  // Explicitly set creation timestamp
                .build();

        // Act & Assert
        Assertions.assertNull(report.getId());
        userReportRepository.persist(report);
        Assertions.assertNotNull(report.getId());
        Assertions.assertNotNull(report.getCreatedAt());
    }

    @Test
    @Transactional
    public void testFindByReportedUserId_shouldReturnReports() {
        // Arrange
        // Create and persist users
        User reportedUser = User.builder()
                .email("reported2@email.com")
                .password("TesT_ASE24W")
                .username("reportedUser2")
                .build();
        userRepository.persistUser(reportedUser);

        User reportingUser1 = User.builder()
                .email("reporting1@email.com")
                .password("TesT_ASE24W")
                .username("reportingUser1")
                .build();
        userRepository.persistUser(reportingUser1);

        User reportingUser2 = User.builder()
                .email("reporting2@email.com")
                .password("TesT_ASE24W")
                .username("reportingUser2")
                .build();
        userRepository.persistUser(reportingUser2);

        // Create and persist multiple reports
        UserReport report1 = UserReport.builder()
                .reportedUser(reportedUser)
                .reportingUser(reportingUser1)
                .reason("First report reason")
                .createdAt(LocalDateTime.now())
                .build();
        userReportRepository.persist(report1);

        UserReport report2 = UserReport.builder()
                .reportedUser(reportedUser)
                .reportingUser(reportingUser2)
                .reason("Second report reason")
                .createdAt(LocalDateTime.now())
                .build();
        userReportRepository.persist(report2);

        // Act
        // Find reports
        List<UserReport> reports = userReportRepository.findByReportedUserId(reportedUser.getId());

        // Assert
        Assertions.assertEquals(2, reports.size());
        Assertions.assertTrue(reports.contains(report1));
        Assertions.assertTrue(reports.contains(report2));
    }

    @Test
    @Transactional
    public void testFindByReportingUserId_shouldReturnReports() {
        // Arrange
        // Create and persist users
        User reportedUser1 = User.builder()
                .email("reported3@email.com")
                .password("TesT_ASE24W")
                .username("reportedUser3")
                .build();
        userRepository.persistUser(reportedUser1);

        User reportedUser2 = User.builder()
                .email("reported4@email.com")
                .password("TesT_ASE24W")
                .username("reportedUser4")
                .build();
        userRepository.persistUser(reportedUser2);

        User reportingUser = User.builder()
                .email("reporting3@email.com")
                .password("TesT_ASE24W")
                .username("reportingUser3")
                .build();
        userRepository.persistUser(reportingUser);

        // Create and persist multiple reports
        UserReport report1 = UserReport.builder()
                .reportedUser(reportedUser1)
                .reportingUser(reportingUser)
                .reason("Report against user 1")
                .createdAt(LocalDateTime.now())
                .build();
        userReportRepository.persist(report1);

        UserReport report2 = UserReport.builder()
                .reportedUser(reportedUser2)
                .reportingUser(reportingUser)
                .reason("Report against user 2")
                .createdAt(LocalDateTime.now())
                .build();
        userReportRepository.persist(report2);

        // Act
        // Find reports
        List<UserReport> reports = userReportRepository.findByReportingUserId(reportingUser.getId());

        // Assert
        Assertions.assertEquals(2, reports.size());
        Assertions.assertTrue(reports.contains(report1));
        Assertions.assertTrue(reports.contains(report2));
    }

    @Test
    @Transactional
    public void testFindByReportedUserId_nonExistentUser_shouldReturnEmptyList() {
        List<UserReport> reports = userReportRepository.findByReportedUserId(999L);
        Assertions.assertTrue(reports.isEmpty());
    }

    @Test
    @Transactional
    public void testFindByReportingUserId_nonExistentUser_shouldReturnEmptyList() {
        List<UserReport> reports = userReportRepository.findByReportingUserId(999L);
        Assertions.assertTrue(reports.isEmpty());
    }

    @Test
    @Transactional
    public void testDeleteUserReport_shouldSucceed() {
        // Arrange
        // Create and persist users
        User reportedUser = User.builder()
                .email("reported5@email.com")
                .password("TesT_ASE24W")
                .username("reportedUser5")
                .build();
        userRepository.persistUser(reportedUser);

        User reportingUser = User.builder()
                .email("reporting4@email.com")
                .password("TesT_ASE24W")
                .username("reportingUser4")
                .build();
        userRepository.persistUser(reportingUser);

        // Create and persist report
        UserReport report = UserReport.builder()
                .reportedUser(reportedUser)
                .reportingUser(reportingUser)
                .reason("Report to be deleted")
                .createdAt(LocalDateTime.now())
                .build();
        userReportRepository.persist(report);
        Assertions.assertNotNull(report.getId());

        // Act
        // Delete the report
        userReportRepository.delete(report);

        // Assert
        // Verify report is deleted
        List<UserReport> reportedUserReports = userReportRepository.findByReportedUserId(reportedUser.getId());
        List<UserReport> reportingUserReports = userReportRepository.findByReportingUserId(reportingUser.getId());
        
        Assertions.assertTrue(reportedUserReports.isEmpty());
        Assertions.assertTrue(reportingUserReports.isEmpty());
    }
}