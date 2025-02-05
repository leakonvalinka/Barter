package at.ac.ase.inso.group02.bartering.dto;

import at.ac.ase.inso.group02.authentication.dto.UserDetailDTO;
import at.ac.ase.inso.group02.messaging.dto.ChatMessageDTO;
import at.ac.ase.inso.group02.views.Views;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeChatDTO {
    private UUID id;

    private Set<ExchangeItemDTO> exchanges;

    @JsonView({Views.Brief.class})
    private UserDetailDTO initiator;

    private boolean confirmationResponsePending = false;

    private ChatMessageDTO mostRecentMessage;

    private Long numberOfUnseenMessages;
}
