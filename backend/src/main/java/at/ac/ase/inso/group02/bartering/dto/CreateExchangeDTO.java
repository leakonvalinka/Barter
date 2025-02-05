package at.ac.ase.inso.group02.bartering.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateExchangeDTO {

    @NotNull
    private Long skillID;

    private Long skillCounterPartID;

    private String forUser;
}
