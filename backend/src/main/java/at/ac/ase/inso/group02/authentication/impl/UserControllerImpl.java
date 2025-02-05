package at.ac.ase.inso.group02.authentication.impl;

import at.ac.ase.inso.group02.authentication.UserController;
import at.ac.ase.inso.group02.authentication.UserService;
import at.ac.ase.inso.group02.authentication.dto.UserDetailDTO;
import at.ac.ase.inso.group02.authentication.dto.UserUpdateDTO;
import at.ac.ase.inso.group02.rating.RatingService;
import at.ac.ase.inso.group02.rating.dto.UserRatingDTO;
import at.ac.ase.inso.group02.rating.views.RatingViews;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;
import at.ac.ase.inso.group02.views.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.quarkus.logging.Log;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UserControllerImpl implements UserController {

    private UserService userService;
    private RatingService ratingService;

    @Override
    @JsonView(Views.Private.class)
    public UserDetailDTO getCurrentUser() {
        UserDetailDTO currentUser = userService.getCurrentUser();
        Log.infov("Fetching current user with username \"{0}\"", currentUser.getUsername());
        return currentUser;
    }

    @Override
    @JsonView(Views.Public.class)
    public UserDetailDTO getUserByUsername(String username) {
        Log.infov("Fetching user with username \"{0}\"", username);
        return userService.getUserByUsername(username);
    }

    @Override
    @JsonView(Views.Private.class)
    public UserDetailDTO updateUser(UserUpdateDTO updateData) {
        return userService.updateUser(updateData);
    }

    @Override
    public void deleteUser() {
        userService.deleteUser();
    }

    @Override
    @JsonView(RatingViews.IncludeByUser.class)
    public PaginatedQueryDTO<UserRatingDTO> getRatingsForUsername(String username, PaginationParamsDTO paginationParamsDTO) {
        Log.infov("Fetching ratings for user with username \"{0}\" and pagination params: {1}",username, paginationParamsDTO);
        return ratingService.getUserRatings(username, paginationParamsDTO);
    }

}
