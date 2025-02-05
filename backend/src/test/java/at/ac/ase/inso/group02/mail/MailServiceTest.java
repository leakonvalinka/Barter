package at.ac.ase.inso.group02.mail;

import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.entities.SkillOffer;
import at.ac.ase.inso.group02.entities.User;
import io.quarkiverse.mailpit.test.WithMailbox;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.ext.mail.MailMessage;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MailServiceTest
 */
@QuarkusTest
@WithMailbox
public class MailServiceTest {
    @Inject
    MockMailbox mailbox;

    @Inject
    MailService mailService;

    @BeforeEach
    public void init() {
        mailbox.clear();
    }

    @Test
    public void testVerifyUserMail_shouldContainVerificationLink() {
        // Arrange
        String verificationLink = "/verify/1312";
        User user = User.builder().email("daniel@fenz.io").build();

        // Act
        mailService.sendAccountVerificationMail(user, verificationLink).await().indefinitely();

        // Assert
        assertEquals(1, mailbox.getTotalMessagesSent());
        MailMessage message = mailbox.getMailMessagesSentTo(user.getEmail()).get(0);

        assertNotNull(message);
        assertEquals(user.getEmail(), message.getTo().get(0));
        assertTrue(message.getHtml().contains(verificationLink));
    }

    @Test
    public void testVerifyUserMailSync_shouldContainVerificationLink() {
        // Arrange
        String verificationLink = "/verify/1312";
        User user = User.builder().email("daniel@fenz.io").build();

        // Act
        mailService.sendAccountVerificationMailSync(user, verificationLink);

        // Assert
        assertEquals(1, mailbox.getTotalMessagesSent());
        MailMessage message = mailbox.getMailMessagesSentTo(user.getEmail()).get(0);

        assertNotNull(message);
        assertEquals(user.getEmail(), message.getTo().get(0));
        assertTrue(message.getHtml().contains(verificationLink));

    }

    @Test
    public void testResetPasswordMail_shouldContainResetLink() {
        // Arrange
        String passwordResetLink = "/reset-password/token-should-go-here";
        User user = User.builder().email("daniel@fenz.io").build();

        // Act
        mailService.sendPasswordResetMail(user.getEmail(), passwordResetLink);

        // Assert
        assertEquals(1, mailbox.getTotalMessagesSent());
        MailMessage message = mailbox.getMailMessagesSentTo(user.getEmail()).get(0);

        assertNotNull(message);
        assertEquals(user.getEmail(), message.getTo().get(0));
        assertTrue(message.getHtml().contains(passwordResetLink));
    }

    @Test
    public void testUserBannedMail_shouldContainBanReason() {
        // Arrange
        String banReason = "Violation of community guidelines";
        User user = User.builder()
            .email("daniel@fenz.io")
            .displayName("Daniel")
            .build();

        // Act
        mailService.sendUserBannedMail(user, banReason).await().indefinitely();

        // Assert
        assertEquals(1, mailbox.getTotalMessagesSent());
        MailMessage message = mailbox.getMailMessagesSentTo(user.getEmail()).get(0);

        assertNotNull(message);
        assertEquals(user.getEmail(), message.getTo().get(0));
        assertEquals("Account Banned", message.getSubject());
        assertTrue(message.getHtml().contains(banReason));
        assertTrue(message.getHtml().contains(user.getDisplayName()));
    }

    @Test
    public void testSkillDeletedMail_shouldContainSkillInfo() {
        // Arrange
        User user = User.builder()
            .email("daniel@fenz.io")
            .displayName("Daniel")
            .build();
            
        Skill skill = SkillOffer.builder()
            .title("Programming Java")
            .description("Teaching Java programming")
            .byUser(user)
            .build();

        // Act
        mailService.sendSkillDeletedMail(user, skill).await().indefinitely();

        // Assert
        assertEquals(1, mailbox.getTotalMessagesSent());
        MailMessage message = mailbox.getMailMessagesSentTo(user.getEmail()).get(0);

        assertNotNull(message);
        assertEquals(user.getEmail(), message.getTo().get(0));
        assertEquals("Skill Deleted", message.getSubject());
        assertTrue(message.getHtml().contains(skill.getTitle()));
        assertTrue(message.getHtml().contains(user.getDisplayName()));
    }
}
