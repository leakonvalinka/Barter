package at.ac.ase.inso.group02.admin;

import at.ac.ase.inso.group02.admin.dtos.*;
import at.ac.ase.inso.group02.admin.exception.UserIsBannedException;
import at.ac.ase.inso.group02.authentication.AuthenticationService;
import at.ac.ase.inso.group02.authentication.UserRepository;
import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.entities.SkillCategory;
import at.ac.ase.inso.group02.entities.SkillOffer;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.admin.*;
import at.ac.ase.inso.group02.mail.MailService;
import at.ac.ase.inso.group02.skills.GenericSkillRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
public class AdminServiceTest {

    @Inject
    AdminService adminService;

    @InjectMock
    UserBanRepository banRepository;

    @InjectMock
    UserReportRepository userReportRepository;

    @InjectMock
    SkillReportRepository skillReportRepository;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    GenericSkillRepository<Skill> skillRepository;

    @InjectMock
    MailService mailService;

    @InjectMock
    AuthenticationService authenticationService;

    private User testUser;
    private User currentUser;
    private Skill testSkill;
    private UserReport testUserReport;
    private SkillReport testSkillReport;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.now();

        testUser = User.builder()
                .id(1L)
                .email("test@email.com")
                .username("testuser")
                .build();

        currentUser = User.builder()
                .id(2L)
                .email("currentUser@email.com")
                .username("TheCurrentUser")
                .build();

        SkillCategory category = SkillCategory.builder()
                .id(1L)
                .name("Test Category")
                .description("Test Description")
                .build();

        testSkill = SkillOffer.builder()
                .id(1L)
                .title("Test Skill")
                .description("Test Description")
                .category(category)
                .byUser(testUser)
                .build();

        testUserReport = UserReport.builder()
                .id(1L)
                .reportedUser(testUser)
                .reportingUser(currentUser)
                .reason("Test report reason")
                .createdAt(testTime)
                .build();

        testSkillReport = SkillReport.builder()
                .id(1L)
                .reportedSkill(testSkill)
                .reportingUser(currentUser)
                .reason("Test skill report reason")
                .status(ReportStatus.PENDING)
                .createdAt(testTime)
                .build();


    }

    @Test
    void testBanValidUser_shouldCreateBanAndSendEmail() {
        // Arrange
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(testUser);
        when(banRepository.isUserBanned(testUser.getId())).thenReturn(false);
        doAnswer(invocation -> {
            UserBan ban = invocation.getArgument(0);
            ban.setId(1L);
            ban.setBannedAt(testTime);
            return null;
        }).when(banRepository).persistAndFlush(any(UserBan.class));

        // Act
        UserBanDTO result = adminService.banUser(testUser.getUsername(), "Test ban reason");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals("Test ban reason", result.getReason());
        assertEquals(testTime, result.getBannedAt());

        ArgumentCaptor<UserBan> banCaptor = ArgumentCaptor.forClass(UserBan.class);
        verify(banRepository).persistAndFlush(banCaptor.capture());
        UserBan capturedBan = banCaptor.getValue();
        assertEquals(testUser, capturedBan.getUser());
        assertEquals("Test ban reason", capturedBan.getReason());

        verify(mailService).sendUserBannedMail(testUser, "Test ban reason");
    }

    @Test
    void testBanNonexistentUser_shouldFailWithNotFoundException() {
        // Arrange
        when(userRepository.findByUsername("NonExistantUser")).thenReturn(null);

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
            adminService.banUser("NonExistantUser", "Test ban reason")
        );
        verify(banRepository, never()).persist(any(UserBan.class));
    }

    @Test
    void testBanAlreadyBannedUser_shouldFailWithIllegalStateException() {
        // Arrange
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(testUser);
        when(banRepository.isUserBanned(1L)).thenReturn(true);

        // Act & Assert
        assertThrows(UserIsBannedException.class, () ->
            adminService.banUser(testUser.getUsername(), "Test ban reason")
        );
        verify(banRepository, never()).persist(any(UserBan.class));
    }

    @Test
    void testReportValidUser_shouldCreateReport() {
        // Arrange
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(testUser);
        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        doAnswer(invocation -> {
            UserReport report = invocation.getArgument(0);
            report.setId(1L);
            report.setCreatedAt(testTime);
            return null;
        }).when(userReportRepository).persistAndFlush(any(UserReport.class));

        // Act
        UserReportDTO result = adminService.reportUser(testUser.getUsername(), "Test report reason");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(testUser.getUsername(), result.getReportedUserUsername());
        assertEquals(currentUser.getUsername(), result.getReportingUserUsername());
        assertEquals("Test report reason", result.getReason());
        assertEquals(testTime, result.getCreatedAt());

        ArgumentCaptor<UserReport> reportCaptor = ArgumentCaptor.forClass(UserReport.class);
        verify(userReportRepository).persistAndFlush(reportCaptor.capture());
        UserReport capturedReport = reportCaptor.getValue();
        assertEquals(testUser, capturedReport.getReportedUser());
        assertEquals(currentUser, capturedReport.getReportingUser());
        assertEquals("Test report reason", capturedReport.getReason());
    }

    @Test
    void testReportNonexistentUser_shouldFailWithNotFoundException() {
        // Arrange
        when(userRepository.findByUsername("NonExistantUser")).thenReturn(null);
        when(userRepository.findById(2L)).thenReturn(currentUser);
        when(authenticationService.getCurrentUser()).thenReturn(currentUser);

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
            adminService.reportUser("NonExistantUser", "Test report reason")
        );
        verify(userReportRepository, never()).persist(any(UserReport.class));
    }

    @Test
    void testReportValidSkillFromValidUser_shouldCreateReport() {
        // Arrange
        when(skillRepository.findById(1L)).thenReturn(testSkill);
        when(userRepository.findById(2L)).thenReturn(currentUser);
        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        doAnswer(invocation -> {
            SkillReport report = invocation.getArgument(0);
            report.setId(1L);
            report.setCreatedAt(testTime);
            return null;
        }).when(skillReportRepository).persistAndFlush(any(SkillReport.class));

        // Act
        SkillReportDTO result = adminService.reportSkill(1L, "Test skill report reason");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(testSkill.getId(), result.getSkillId());
        assertEquals(currentUser.getUsername(), result.getReportingUserUsername());
        assertEquals("Test skill report reason", result.getReason());
        assertEquals(ReportStatus.PENDING, result.getStatus());
        assertEquals(testTime, result.getCreatedAt());

        ArgumentCaptor<SkillReport> reportCaptor = ArgumentCaptor.forClass(SkillReport.class);
        verify(skillReportRepository).persistAndFlush(reportCaptor.capture());
        SkillReport capturedReport = reportCaptor.getValue();
        assertEquals(testSkill, capturedReport.getReportedSkill());
        assertEquals(currentUser, capturedReport.getReportingUser());
        assertEquals("Test skill report reason", capturedReport.getReason());
    }

    @Test
    void testUpdateValidSkillReportStatus_shouldUpdateStatus() {
        // Arrange
        when(skillReportRepository.findById(1L)).thenReturn(testSkillReport);

        // Act
        SkillReportDTO result = adminService.updateSkillReportStatus(1L, ReportStatus.APPROVED);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(ReportStatus.APPROVED, result.getStatus());
        assertNotNull(result.getResolvedAt());

        assertEquals(ReportStatus.APPROVED, testSkillReport.getStatus());
        assertNotNull(testSkillReport.getResolvedAt());
    }

    @Test
    void testUpdateNonexistentSkillReportStatus_shouldFailWithNotFoundException() {
        // Arrange
        when(skillReportRepository.findById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
            adminService.updateSkillReportStatus(999L, ReportStatus.APPROVED)
        );
    }

    @Test
    void testApproveSkillReportStatus_shouldSendEmailAndDeleteSkill() {
        // Arrange
        when(skillReportRepository.findById(1L)).thenReturn(testSkillReport);
        when(mailService.sendSkillDeletedMail(any(), any())).thenReturn(Uni.createFrom().voidItem());

        // Act
        adminService.updateSkillReportStatus(1L, ReportStatus.APPROVED);

        // Assert
        verify(mailService).sendSkillDeletedMail(testUser, testSkill);
        verify(skillRepository).delete(testSkill);
    }

    @Test
    void testApproveSkillReportStatus_shouldDeleteSkillAndReport() {
        // Arrange
        when(skillReportRepository.findById(1L)).thenReturn(testSkillReport);
        when(mailService.sendSkillDeletedMail(any(), any())).thenReturn(Uni.createFrom().voidItem());

        // Act
        adminService.updateSkillReportStatus(1L, ReportStatus.APPROVED);

        // Assert
        var inOrder = inOrder(mailService, skillRepository, skillReportRepository);
        inOrder.verify(mailService).sendSkillDeletedMail(testUser, testSkill);
        inOrder.verify(skillRepository).delete(testSkill);
//        inOrder.verify(skillReportRepository).delete(testSkillReport); // this is done automatically
    }

    @Test
    void testUpdateSkillReportStatusNotResolved_shouldNotSendEmail() {
        // Arrange
        when(skillReportRepository.findById(1L)).thenReturn(testSkillReport);

        // Act
        adminService.updateSkillReportStatus(1L, ReportStatus.REJECTED);

        // Assert
        verify(mailService, never()).sendSkillDeletedMail(any(), any());
        verify(skillRepository, never()).delete(any());
    }

    @Test
    void isUserBanned_shouldReturnCorrectStatus() {
        // Arrange
        when(banRepository.isUserBanned(testUser.getId())).thenReturn(true);
        PanacheQuery mockQuery = mock(PanacheQuery.class);
        when(mockQuery.firstResult()).thenReturn(testUser);
        when(userRepository.find("username", testUser.getUsername())).thenReturn(mockQuery);

        // Act
        BanStatusDTO result = adminService.isUserBanned(testUser.getUsername());

        // Assert
        assertTrue(result.isBanned());
        verify(banRepository).isUserBanned(1L);
    }

    @Test
    void testGetUserReports_shouldReturnReports() {
        // Arrange
        List<UserReport> reports = Arrays.asList(testUserReport);
        when(userReportRepository.findByReportedUserId(testUser.getId())).thenReturn(reports);
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(testUser);

        // Act
        List<UserReportDTO> result = adminService.getUserReports(testUser.getUsername());

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        UserReportDTO dto = result.get(0);
        assertEquals(1L, dto.getId());
        assertEquals(testUser.getUsername(), dto.getReportedUserUsername());
        assertEquals(currentUser.getUsername(), dto.getReportingUserUsername());
        assertEquals("Test report reason", dto.getReason());
        assertEquals(testTime, dto.getCreatedAt());
    }

    @Test
    void testGetSkillReports_shouldReturnReports() {
        // Arrange
        List<SkillReport> reports = Arrays.asList(testSkillReport);
        when(skillReportRepository.findBySkillId(1L)).thenReturn(reports);

        // Act
        List<SkillReportDTO> result = adminService.getSkillReports(1L);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        SkillReportDTO dto = result.get(0);
        assertEquals(1L, dto.getId());
        assertEquals(testSkill.getId(), dto.getSkillId());
        assertEquals(currentUser.getUsername(), dto.getReportingUserUsername());
        assertEquals("Test skill report reason", dto.getReason());
        assertEquals(ReportStatus.PENDING, dto.getStatus());
        assertEquals(testTime, dto.getCreatedAt());
    }

    @Test
    void TestGetPendingSkillReports_shouldReturnPendingReports() {
        // Arrange
        List<SkillReport> reports = Arrays.asList(testSkillReport);
        when(skillReportRepository.findByStatus(ReportStatus.PENDING)).thenReturn(reports);

        // Act
        List<SkillReportDTO> result = adminService.getPendingSkillReports();

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        SkillReportDTO dto = result.get(0);
        assertEquals(1L, dto.getId());
        assertEquals(testSkill.getId(), dto.getSkillId());
        assertEquals(ReportStatus.PENDING, dto.getStatus());
        assertEquals(testTime, dto.getCreatedAt());
    }
}