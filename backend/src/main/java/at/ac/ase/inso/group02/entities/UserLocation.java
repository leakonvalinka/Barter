package at.ac.ase.inso.group02.entities;

import com.bedatadriven.jackson.datatype.jts.serialization.GeometryDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import org.locationtech.jts.geom.Point;


/**
 * represents users home location.
 * Note that this location might not be exact to protect the user's privacy
 */
@Embeddable
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class UserLocation {

    private String street;
    private String streetNumber;
    private String city;
    private Integer postalCode;
    private String country;


    @Column(columnDefinition = "geography(Point,4326)")
    @JsonDeserialize(contentUsing = GeometryDeserializer.class)
    public Point homeLocation;
}
