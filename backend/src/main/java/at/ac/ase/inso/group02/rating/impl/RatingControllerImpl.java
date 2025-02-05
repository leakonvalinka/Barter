package at.ac.ase.inso.group02.rating.impl;

import at.ac.ase.inso.group02.rating.RatingController;
import at.ac.ase.inso.group02.rating.RatingService;
import at.ac.ase.inso.group02.rating.dto.CreateRatingDTO;
import at.ac.ase.inso.group02.rating.dto.UserRatingDTO;
import at.ac.ase.inso.group02.rating.views.RatingViews;
import com.fasterxml.jackson.annotation.JsonView;
import io.quarkus.logging.Log;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RatingControllerImpl implements RatingController {
    private final RatingService ratingService;

    @Override
    @JsonView(RatingViews.IncludeForUser.class)
    public UserRatingDTO updateRating(Long ratingID, CreateRatingDTO ratingDTO) {
        Log.infov("Updating rating with id {0} and values: {1}",ratingID, ratingDTO);
        return ratingService.updateRating(ratingID, ratingDTO);
    }

    @Override
    public void deleteRating(Long ratingID) {
        Log.infov("Deleting rating with id {0}", ratingID);
        ratingService.deleteRating(ratingID);
    }
}
