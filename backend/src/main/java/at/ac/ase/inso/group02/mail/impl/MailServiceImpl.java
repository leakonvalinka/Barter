package at.ac.ase.inso.group02.mail.impl;

import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.mail.MailService;
import io.quarkus.logging.Log;
import io.quarkus.mailer.MailTemplate.MailTemplateInstance;
import io.quarkus.qute.CheckedTemplate;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Service to send mails
 */
@ApplicationScoped
public class MailServiceImpl implements MailService {
    @ConfigProperty(name = "barter.url")
    private String barterUrl;

    @ConfigProperty(name = "barter.verify-route")
    private String verifyRoute;

    @CheckedTemplate
    private static class Templates {
        public static native MailTemplateInstance verifyUser(User user, String verificationCode, String verifyUrl, String baseUrl);

        public static native MailTemplateInstance resetPassword(String email, String resetToken, String url);

        public static native MailTemplateInstance userBanned(User user, String reason, String url);
        
        public static native MailTemplateInstance skillDeleted(User user, Skill skill, String url);
    }

    public Uni<Void> sendAccountVerificationMail(User user, String verificationCode) {
        Log.infov("Sending verification email to {0}", user.getEmail());
        return Templates.verifyUser(user, verificationCode, barterUrl + verifyRoute, barterUrl)
                .to(user.getEmail())
                .subject("Account Verification")
                .send();
    }

    public void sendAccountVerificationMailSync(User user, String verificationCode) {
        sendAccountVerificationMail(user, verificationCode)
                .await()
                .indefinitely();
    }

    @Override
    public void sendPasswordResetMail(String email, String resetToken) {
        Log.infov("Sending password reset email to {0}", email);
        Templates.resetPassword(email, resetToken, barterUrl)
                .to(email)
                .subject("Password Reset")
                .sendAndAwait();
    }

    @Override
    public Uni<Void> sendUserBannedMail(User user, String reason) {
        Log.infov("Sending ban notification email to {0}", user.getEmail());
        return Templates.userBanned(user, reason, barterUrl)
                .to(user.getEmail())
                .subject("Account Banned")
                .send();
    }

    @Override
    public Uni<Void> sendSkillDeletedMail(User user, Skill skill) {
        Log.infov("Sending skill deletion notification to {0}", user.getEmail());
        return Templates.skillDeleted(user, skill, barterUrl)
                .to(user.getEmail())
                .subject("Skill Deleted")
                .send();
    }
}