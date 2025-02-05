package at.ac.ase.inso.group02.entities.messaging;

import at.ac.ase.inso.group02.entities.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * WebSocket one-time-use ticket for authenticated connection establishment
 */
@Entity
@Table(name = "ws_ticket")
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class WSTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID ticketUUID;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference(value = "ws-tickets")
    private User forUser;
}
