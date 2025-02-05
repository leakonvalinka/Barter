package at.ac.ase.inso.group02.admin.impl;

import at.ac.ase.inso.group02.admin.AdminService;
import at.ac.ase.inso.group02.admin.SkillReportRepository;
import at.ac.ase.inso.group02.admin.UserBanRepository;
import at.ac.ase.inso.group02.admin.UserReportRepository;
import at.ac.ase.inso.group02.admin.dtos.*;
import at.ac.ase.inso.group02.admin.exception.UserIsBannedException;
import at.ac.ase.inso.group02.authentication.AuthenticationService;
import at.ac.ase.inso.group02.authentication.UserRepository;
import at.ac.ase.inso.group02.entities.*;
import at.ac.ase.inso.group02.entities.admin.*;
import at.ac.ase.inso.group02.mail.MailService;
import at.ac.ase.inso.group02.skills.GenericSkillRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.DiscriminatorValue;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@AllArgsConstructor
public class AdminServiceImpl implements AdminService {

    private UserBanRepository banRepository;

    private UserReportRepository userReportRepository;

    private SkillReportRepository skillReportRepository;

    private UserRepository userRepository;

    private GenericSkillRepository<Skill> skillRepository;

    private MailService mailService;

    private AuthenticationService authenticationService;

    @Override
    @Transactional
    public UserBanDTO banUser(String username, String reason) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        if (banRepository.isUserBanned(user.getId())) {
            throw new UserIsBannedException("User " + user.getDisplayName() + " is already banned");
        }

        // Delete all user's skills first
        List<Skill> userSkills = skillRepository.findByUser(user);
        for (Skill skill : userSkills) {
            // Send notification for each deleted skill
            skillRepository.delete(skill);
        }

        UserBan ban = UserBan.builder()
            .user(user)
            .reason(reason)
            .build();

        banRepository.persistAndFlush(ban);

        mailService.sendUserBannedMail(user, reason).subscribeAsCompletionStage();
        
        return this.mapUserBan(ban);
    }

    @Override
    @Transactional
    public UserReportDTO reportUser(String reportedUserUsername, String reason) {
        User reportedUser = userRepository.findByUsername(reportedUserUsername);
        User reportingUser = authenticationService.getCurrentUser();
        
        if (reportedUser == null || reportingUser == null) {
            throw new NotFoundException("User not found");
        }

        UserReport report = UserReport.builder()
        .reportedUser(reportedUser)
        .reportingUser(reportingUser)
        .reason(reason)
        .build();

        userReportRepository.persistAndFlush(report);

        return this.mapUserReport(report);
    }

    @Override
    @Transactional
    public SkillReportDTO reportSkill(Long skillId, String reason) {
        Skill skill = skillRepository.findById(skillId);
        User reportingUser = authenticationService.getCurrentUser();
        
        if (skill == null || reportingUser == null) {
            throw new NotFoundException("Skill or User not found");
        }

        SkillReport report = SkillReport.builder()
        .reportedSkill(skill)
        .reportingUser(reportingUser)
        .reason(reason)
        .build();

        skillReportRepository.persistAndFlush(report);

        return this.mapSkillReport(report);
    }

    @Override
    @Transactional
    public SkillReportDTO updateSkillReportStatus(Long reportId, ReportStatus newStatus) {
        SkillReport report = skillReportRepository.findById(reportId);
        if (report == null) {
            throw new NotFoundException("Report not found");
        }

        report.setStatus(newStatus);
        report.setResolvedAt(LocalDateTime.now());

        // If the skill is being deleted as a result of the report
        if (newStatus == ReportStatus.APPROVED && report.getReportedSkill() != null) {
            Skill skill = report.getReportedSkill();
            User skillOwner = skill.getByUser();
            
            // Send notification before deleting the skill
            mailService.sendSkillDeletedMail(skillOwner, skill)
                .subscribeAsCompletionStage();
                
            // Delete the skill, will automatically delete the report
            skillRepository.delete(skill);
        } else {
            // Persist if not approved
            skillReportRepository.persistAndFlush(report);
        }

        return this.mapSkillReport(report);
    }

    @Override
    public List<UserReportDTO> getUserReports(String username) {
        User user = userRepository.findByUsername(username);

        if(user == null) {
            throw new NotFoundException("User not found");
        }

        return this.mapUserReports(
            userReportRepository.findByReportedUserId(user.getId())
        );
    }

    @Override
    public List<SkillReportDTO> getSkillReports(Long skillId) {
        return this.mapSkillReports(
            skillReportRepository.findBySkillId(skillId)
        );
    }

    @Override
    public List<SkillReportDTO> getPendingSkillReports() {
        return this.mapSkillReports(
            skillReportRepository.findByStatus(ReportStatus.PENDING)
        );
    }

    @Override
    public List<UserReportDTO> getPendingUserReports() {
        return this.mapUserReports(
            userReportRepository.findByStatus(ReportStatus.PENDING)
        );
    }

    @Override
    public List<UserReportDTO> getAllUserReports() {
        return this.mapUserReports(
            userReportRepository.findAll().list()
        );
    }

    @Override
    public List<SkillReportDTO> getAllSkillReports() {
        return this.mapSkillReports(
            skillReportRepository.findAll().list()
        );
    }

    @Override
    public BanStatusDTO isUserBanned(String username) {
        // First check if user exists at all, without considering ban status
        User user = userRepository.find("username", username).firstResult();
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        
        // Now check if the user is banned
        return BanStatusDTO.builder()
            .banned(banRepository.isUserBanned(user.getId()))
            .build();
    }

    @Override
    @Transactional
    public void deleteUserReport(Long reportId) {
        UserReport report = userReportRepository.findById(reportId);
        if (report == null) {
            throw new NotFoundException("Report not found");
        }
        userReportRepository.delete(report);
    }

    private UserBanDTO mapUserBan(UserBan userBan) {
        return UserBanDTO.builder()
            .id(userBan.getId())
            .username(userBan.getUser().getUsername())
            .reason(userBan.getReason())
            .bannedAt(userBan.getBannedAt())
            .build();
    }

    private UserReportDTO mapUserReport(UserReport userReport) {
        return UserReportDTO.builder()
        .id(userReport.getId())
        .createdAt(userReport.getCreatedAt())
        .reason(userReport.getReason())
        .reportedUserUsername(userReport.getReportedUser().getUsername())
        .reportingUserUsername(userReport.getReportingUser().getUsername())
        .build();
    }

    private List<UserReportDTO> mapUserReports(List<UserReport> userReports) {
        return userReports.stream()
            .map(this::mapUserReport)
            .collect(Collectors.toList());
    }

    private SkillReportDTO mapSkillReport(SkillReport skillReport) {
        Skill skill = skillReport.getReportedSkill();
        String skillType = "UNKNOWN";
        
        if (skill != null) {
            DiscriminatorValue discriminatorValue = skill.getClass().getAnnotation(DiscriminatorValue.class);
            if (discriminatorValue != null) {
                skillType = discriminatorValue.value();
            } else {
                // Fallback to class name based determination
                skillType = skill.getClass().getSimpleName().contains("Demand") ? "DEMAND" : "OFFER";
            }
        }

        return SkillReportDTO.builder()
        .id(skillReport.getId())
        .reason(skillReport.getReason())
        .createdAt(skillReport.getCreatedAt())
        .resolvedAt(skillReport.getResolvedAt())
        .reportingUserUsername(skillReport.getReportingUser().getUsername())
        .skillId(skillReport.getReportedSkill().getId())
        .status(skillReport.getStatus())
        .skillType(skillType)
        .build();
    }

    private List<SkillReportDTO> mapSkillReports(List<SkillReport> skillReports) {
        return skillReports.stream()
            .map(this::mapSkillReport)
            .collect(Collectors.toList());
    }
}