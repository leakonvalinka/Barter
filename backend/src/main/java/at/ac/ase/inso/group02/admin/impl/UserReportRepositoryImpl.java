package at.ac.ase.inso.group02.admin.impl;

import java.util.List;

import at.ac.ase.inso.group02.admin.UserReportRepository;
import at.ac.ase.inso.group02.entities.admin.UserReport;
import at.ac.ase.inso.group02.entities.admin.ReportStatus;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserReportRepositoryImpl implements UserReportRepository {
    @Override
    public List<UserReport> findByReportedUserId(Long userId) {
        return list("reportedUser.id", userId);
    }

    @Override
    public List<UserReport> findByReportingUserId(Long userId) {
        return list("reportingUser.id", userId);
    }

    @Override
    public List<UserReport> findByStatus(ReportStatus status) {
        return list("status", status);
    }
}