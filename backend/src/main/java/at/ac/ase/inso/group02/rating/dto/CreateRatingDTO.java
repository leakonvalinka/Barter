package at.ac.ase.inso.group02.rating.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateRatingDTO {

    @Min(0)
    @Max(10)
    private int ratingHalfStars;

    @Size(max = 100)
    private String title;

    @Size(max = 500)
    private String description;
}
