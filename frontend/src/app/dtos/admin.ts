export interface UserReportDTO {
    id: number;
    createdAt: Date;
    reason: string;
    reportedUserUsername: string;
    reportingUserUsername: string;
}

export interface SkillReportDTO {
    id: number;
    reason: string;
    createdAt: Date;
    resolvedAt: Date | null;
    reportingUserUsername: string;
    skillId: number;
    skillType: string;  // 'DEMAND' or 'OFFER'
    status: ReportStatus;
}

export interface UserBanDTO {
    id: number;
    username: string;
    reason: string;
    bannedAt: Date;
}

export interface BanStatusDTO {
    banned: boolean;
}

export enum ReportStatus {
    PENDING = 'PENDING',
    APPROVED = 'APPROVED',
    REJECTED = 'REJECTED'
}