package at.ac.ase.inso.group02.mail;

import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.entities.User;
import io.smallrye.mutiny.Uni;

/**
 * MailService
 */
public interface MailService {

    /**
     * Send a password reset mail to a user.
     *
     * @param resetToken the reset token
     * @param email      the email of the user to which the email should be sent
     */
    void sendPasswordResetMail(String email, String resetToken);

    /**
     * Send a verification mail to a user
     *
     * @param verificationPath the path with the verification information
     * @param user             the user to which the email should be sent to
     */
    Uni<Void> sendAccountVerificationMail(User user, String verificationCode);

    /**
     * Send a verification mail to a user and block
     *
     * @param verificationPath the path with the verification information
     * @param user             the user to which the email should be sent to
     */
    void sendAccountVerificationMailSync(User user, String verificationCode);

    /**
     * Send a notification email to a user who has been banned
     *
     * @param user   the banned user
     * @param reason the reason for the ban
     */
    Uni<Void> sendUserBannedMail(User user, String reason);

    /**
     * Send a notification email to a user whose reported skill has been deleted
     *
     * @param user  the user who owned the skill
     * @param skill the skill that was deleted
     */
    Uni<Void> sendSkillDeletedMail(User user, Skill skill);
}
