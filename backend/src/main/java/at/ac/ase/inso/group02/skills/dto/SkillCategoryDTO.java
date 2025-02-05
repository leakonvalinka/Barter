package at.ac.ase.inso.group02.skills.dto;

import at.ac.ase.inso.group02.views.Views;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkillCategoryDTO {
    private Long id;

    @JsonProperty(required = false)
    @JsonView({Views.Full.class, Views.Public.class})
    private String name;

    @JsonProperty(required = false)
    @JsonView({Views.Full.class, Views.Public.class})
    private String description;
}
