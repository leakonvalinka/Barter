package at.ac.ase.inso.group02.messaging.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.jboss.resteasy.reactive.RestQuery;

@Data
public class ChatQueryParamDTO {
    @RestQuery
    @Min(0)
    Long count = 20L;

    @RestQuery
    String beforeMessageUUID;
}
