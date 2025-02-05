package at.ac.ase.inso.group02.admin.impl;

import java.util.List;

import at.ac.ase.inso.group02.admin.SkillReportRepository;
import at.ac.ase.inso.group02.entities.admin.ReportStatus;
import at.ac.ase.inso.group02.entities.admin.SkillReport;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SkillReportRepositoryImpl implements SkillReportRepository {
    @Override
    public List<SkillReport> findBySkillId(Long skillId) {
        return list("reportedSkill.id", skillId);
    }

    @Override
    public List<SkillReport> findByStatus(ReportStatus status) {
        return list("status", status);
    }

    @Override
    public List<SkillReport> findPendingBySkillId(Long skillId) {
        return list("reportedSkill.id = ?1 and status = ?2", skillId, ReportStatus.PENDING);
    }
}