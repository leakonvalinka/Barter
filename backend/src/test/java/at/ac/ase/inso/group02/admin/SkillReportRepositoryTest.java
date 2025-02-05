package at.ac.ase.inso.group02.admin;

import at.ac.ase.inso.group02.authentication.UserRepository;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.entities.SkillCategory;
import at.ac.ase.inso.group02.entities.SkillOffer;
import at.ac.ase.inso.group02.entities.admin.ReportStatus;
import at.ac.ase.inso.group02.entities.admin.SkillReport;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
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
public class SkillReportRepositoryTest {

    @Inject
    SkillReportRepository skillReportRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    PanacheRepository<SkillCategory> skillCategoryRepository;

    @Inject
    PanacheRepository<Skill> skillRepository;

    @Inject
    TransactionManager transactionManager;

    @Test
    @Transactional
    public void testPersistSkillReport_shouldSucceedAndSetId() {
        // Arrange
        // Create and persist skill category
        SkillCategory category = SkillCategory.builder()
                .name("Test Category")
                .description("Test Description")
                .build();
        skillCategoryRepository.persist(category);

        // Create and persist user
        User user = User.builder()
                .email("skill@email.com")
                .password("TesT_ASE24W")
                .username("skillUser")
                .build();
        userRepository.persistUser(user);

        // Create and persist skill
        Skill skill = SkillOffer.builder()
                .title("Test Skill")
                .description("Test Description")
                .category(category)
                .byUser(user)
                .build();
        skillRepository.persist(skill);

        // Create and persist reporting user
        User reportingUser = User.builder()
                .email("reporting@email.com")
                .password("TesT_ASE24W")
                .username("reportingUser")
                .build();
        userRepository.persistUser(reportingUser);

        // Create skill report
        SkillReport report = SkillReport.builder()
                .reportedSkill(skill)
                .reportingUser(reportingUser)
                .reason("Test report reason")
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Assertions.assertNull(report.getId());

        // Act
        skillReportRepository.persist(report);

        // Assert
        Assertions.assertNotNull(report.getId());
        Assertions.assertNotNull(report.getCreatedAt());
        Assertions.assertEquals(ReportStatus.PENDING, report.getStatus());
    }

    @Test
    @Transactional
    public void testFindBySkillId_shouldReturnReports() {
        // Arrange
        // Create necessary entities
        SkillCategory category = SkillCategory.builder()
                .name("Find Category")
                .description("Find Description")
                .build();
        skillCategoryRepository.persist(category);

        User skillUser = User.builder()
                .email("skill2@email.com")
                .password("TesT_ASE24W")
                .username("skillUser2")
                .build();
        userRepository.persistUser(skillUser);

        Skill skill = SkillOffer.builder()
                .title("Find Skill")
                .description("Find Description")
                .category(category)
                .byUser(skillUser)
                .build();
        skillRepository.persist(skill);

        // Create two reporting users
        User reporter1 = User.builder()
                .email("reporter1@email.com")
                .password("TesT_ASE24W")
                .username("reporter1")
                .build();
        userRepository.persistUser(reporter1);

        User reporter2 = User.builder()
                .email("reporter2@email.com")
                .password("TesT_ASE24W")
                .username("reporter2")
                .build();
        userRepository.persistUser(reporter2);

        // Create and persist two reports with different statuses
        SkillReport report1 = SkillReport.builder()
                .reportedSkill(skill)
                .reportingUser(reporter1)
                .reason("First report")
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        skillReportRepository.persist(report1);

        SkillReport report2 = SkillReport.builder()
                .reportedSkill(skill)
                .reportingUser(reporter2)
                .reason("Second report")
                .status(ReportStatus.APPROVED)
                .createdAt(LocalDateTime.now())
                .resolvedAt(LocalDateTime.now())
                .build();
        skillReportRepository.persist(report2);

        // Act
        List<SkillReport> reports = skillReportRepository.findBySkillId(skill.getId());

        // Assert
        Assertions.assertEquals(2, reports.size());
        Assertions.assertTrue(reports.contains(report1));
        Assertions.assertTrue(reports.contains(report2));
    }

    @Test
    @Transactional
    public void testFindByStatus_shouldReturnReports() {
        // Arrange
        // Create necessary entities
        SkillCategory category = SkillCategory.builder()
                .name("Status Category")
                .description("Status Description")
                .build();
        skillCategoryRepository.persist(category);

        User skillUser = User.builder()
                .email("skill3@email.com")
                .password("TesT_ASE24W")
                .username("skillUser3")
                .build();
        userRepository.persistUser(skillUser);

        Skill skill = SkillOffer.builder()
                .title("Status Skill")
                .description("Status Description")
                .category(category)
                .byUser(skillUser)
                .build();
        skillRepository.persist(skill);

        User reportingUser = User.builder()
                .email("reporter3@email.com")
                .password("TesT_ASE24W")
                .username("reporter3")
                .build();
        userRepository.persistUser(reportingUser);

        // Create reports with different statuses
        SkillReport pendingReport = SkillReport.builder()
                .reportedSkill(skill)
                .reportingUser(reportingUser)
                .reason("Pending report")
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        skillReportRepository.persist(pendingReport);

        SkillReport approvedReport = SkillReport.builder()
                .reportedSkill(skill)
                .reportingUser(reportingUser)
                .reason("Approved report")
                .status(ReportStatus.APPROVED)
                .createdAt(LocalDateTime.now())
                .resolvedAt(LocalDateTime.now())
                .build();
        skillReportRepository.persist(approvedReport);

        SkillReport rejectedReport = SkillReport.builder()
                .reportedSkill(skill)
                .reportingUser(reportingUser)
                .reason("Rejected report")
                .status(ReportStatus.REJECTED)
                .createdAt(LocalDateTime.now())
                .resolvedAt(LocalDateTime.now())
                .build();
        skillReportRepository.persist(rejectedReport);

        // Act
        List<SkillReport> pendingReports = skillReportRepository.findByStatus(ReportStatus.PENDING);
        // Assert
        Assertions.assertTrue(pendingReports.contains(pendingReport));
        Assertions.assertFalse(pendingReports.contains(approvedReport));
        Assertions.assertFalse(pendingReports.contains(rejectedReport));

        // Act
        List<SkillReport> approvedReports = skillReportRepository.findByStatus(ReportStatus.APPROVED);
        // Assert
        Assertions.assertTrue(approvedReports.contains(approvedReport));
        Assertions.assertFalse(approvedReports.contains(pendingReport));
        Assertions.assertFalse(approvedReports.contains(rejectedReport));

        // Act
        List<SkillReport> rejectedReports = skillReportRepository.findByStatus(ReportStatus.REJECTED);
        // Assert
        Assertions.assertTrue(rejectedReports.contains(rejectedReport));
        Assertions.assertFalse(rejectedReports.contains(pendingReport));
        Assertions.assertFalse(rejectedReports.contains(approvedReport));
    }

    @Test
    @Transactional
    public void testFindPendingBySkillId_shouldReturnOnlyPendingReports() {
        // Arrange
        // Create necessary entities
        SkillCategory category = SkillCategory.builder()
                .name("Pending Category")
                .description("Pending Description")
                .build();
        skillCategoryRepository.persist(category);

        User skillUser = User.builder()
                .email("skill4@email.com")
                .password("TesT_ASE24W")
                .username("skillUser4")
                .build();
        userRepository.persistUser(skillUser);

        Skill skill = SkillOffer.builder()
                .title("Pending Skill")
                .description("Pending Description")
                .category(category)
                .byUser(skillUser)
                .build();
        skillRepository.persist(skill);

        User reportingUser = User.builder()
                .email("reporter4@email.com")
                .password("TesT_ASE24W")
                .username("reporter4")
                .build();
        userRepository.persistUser(reportingUser);

        // Create reports with different statuses
        SkillReport pendingReport = SkillReport.builder()
                .reportedSkill(skill)
                .reportingUser(reportingUser)
                .reason("Pending report")
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        skillReportRepository.persist(pendingReport);

        SkillReport approvedReport = SkillReport.builder()
                .reportedSkill(skill)
                .reportingUser(reportingUser)
                .reason("Approved report")
                .status(ReportStatus.APPROVED)
                .createdAt(LocalDateTime.now())
                .resolvedAt(LocalDateTime.now())
                .build();
        skillReportRepository.persist(approvedReport);

        SkillReport rejectedReport = SkillReport.builder()
                .reportedSkill(skill)
                .reportingUser(reportingUser)
                .reason("Rejected report")
                .status(ReportStatus.REJECTED)
                .createdAt(LocalDateTime.now())
                .resolvedAt(LocalDateTime.now())
                .build();
        skillReportRepository.persist(rejectedReport);

        // Act
        List<SkillReport> pendingReports = skillReportRepository.findPendingBySkillId(skill.getId());
        // Assert
        Assertions.assertEquals(1, pendingReports.size());
        Assertions.assertTrue(pendingReports.contains(pendingReport));
        Assertions.assertFalse(pendingReports.contains(approvedReport));
        Assertions.assertFalse(pendingReports.contains(rejectedReport));
    }

    @Test
    @Transactional
    public void testFindBySkillId_nonExistentSkill_shouldReturnEmptyList() {
        // Act
        List<SkillReport> reports = skillReportRepository.findBySkillId(999L);
        // Assert
        Assertions.assertTrue(reports.isEmpty());
    }

    @Test
    @Transactional
    public void testFindByStatus_nonExistentReports_shouldReturnEmptyList() {
        // Act
        List<SkillReport> pendingReports = skillReportRepository.findByStatus(ReportStatus.PENDING);
        List<SkillReport> approvedReports = skillReportRepository.findByStatus(ReportStatus.APPROVED);
        List<SkillReport> rejectedReports = skillReportRepository.findByStatus(ReportStatus.REJECTED);

        // Assert
        Assertions.assertTrue(pendingReports.isEmpty());
        Assertions.assertTrue(approvedReports.isEmpty());
        Assertions.assertTrue(rejectedReports.isEmpty());
    }

    @Test
    @Transactional
    public void testFindPendingBySkillId_nonExistentSkill_shouldReturnEmptyList() {
        // Act
        List<SkillReport> reports = skillReportRepository.findPendingBySkillId(999L);
        // Assert
        Assertions.assertTrue(reports.isEmpty());
    }
}