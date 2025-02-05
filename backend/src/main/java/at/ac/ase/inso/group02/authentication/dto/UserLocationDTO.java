package at.ac.ase.inso.group02.authentication.dto;

import at.ac.ase.inso.group02.views.Views;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.locationtech.jts.geom.Point;

/**
 * DTO for the address of the user.
 */
@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserLocationDTO {

    @JsonView(Views.Private.class)
    private String street;

    @JsonView(Views.Private.class)
    private String streetNumber;

    @JsonView(Views.Private.class)
    private String city;

    @JsonView(Views.Private.class)
    private Integer postalCode;

    @JsonView(Views.Private.class)
    private String country;

    @JsonProperty("homeLocation")
    @Schema(
            description = "GeoJSON representation of the home address as a Point",
            example = "{\"type\": \"Point\", \"coordinates\": [0, 0]}",
            implementation = Object.class // Prevents the detailed JTS structure
    )
    private Point homeLocation; // represented as GeoJSON
}
