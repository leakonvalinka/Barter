package at.ac.ase.inso.group02.bartering.dto;

import at.ac.ase.inso.group02.messaging.dto.NewChatMessageDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.Set;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class InitiateExchangesDTO {
    @Size(min = 1, max = 100)
    @NotNull
    Set<CreateExchangeDTO> exchanges;

    @NotNull
    @Valid
    NewChatMessageDTO chatMessage;
}
