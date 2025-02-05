package at.ac.ase.inso.group02.authentication.dto;

import at.ac.ase.inso.group02.views.Views;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfoDTO {

    @JsonView(Views.Private.class)
    private String email;
    private String username;
    private String displayName;

    private String profilePicture;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    private Double averageRatingHalfStars;
}
