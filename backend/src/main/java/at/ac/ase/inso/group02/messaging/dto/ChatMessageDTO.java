package at.ac.ase.inso.group02.messaging.dto;

import at.ac.ase.inso.group02.authentication.dto.UserInfoDTO;
import at.ac.ase.inso.group02.entities.messaging.MessageReadState;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessageDTO {
    private String id;
    private String exchangeID;
    private String content;

    @Builder.Default
    private Boolean exchangeChanged = false;

    private UserInfoDTO author;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime timestamp;

    @Nullable // when the message is not authored by the current user, this is null
    private MessageReadState readState;
}
