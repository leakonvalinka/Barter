package at.ac.ase.inso.group02.skills.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.hibernate.validator.constraints.Length;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.DEDUCTION;

@Data
@SuperBuilder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateSkillDemandDTO.class, name = "SkillDemand"),
        @JsonSubTypes.Type(value = CreateSkillOfferDTO.class, name = "SkillOffer")
})
public class CreateSkillDTO {

    @NotNull
    @NotBlank
    @Length(min = 5, max = 100)
    private String title;

    @NotNull
    @NotBlank
    @Length(min = 10, max = 2000)
    private String description;

    @NotNull
    @Valid
    private SkillCreateCategoryDTO category;

    @Data
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SkillCreateCategoryDTO {
        @NotNull
        private Long id;
    }
}
