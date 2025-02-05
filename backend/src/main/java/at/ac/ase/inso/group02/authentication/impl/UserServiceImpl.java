package at.ac.ase.inso.group02.authentication.impl;

import at.ac.ase.inso.group02.authentication.AuthenticationService;
import at.ac.ase.inso.group02.authentication.UserRepository;
import at.ac.ase.inso.group02.authentication.UserService;
import at.ac.ase.inso.group02.authentication.dto.UserDetailDTO;
import at.ac.ase.inso.group02.authentication.dto.UserUpdateDTO;
import at.ac.ase.inso.group02.entities.Image;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.UserLocation;
import at.ac.ase.inso.group02.images.ImageRepository;
import at.ac.ase.inso.group02.util.MapperUtil;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private AuthenticationService authenticationService;

    private ImageRepository imageRepository;

    private UserRepository userRepository;

    @Override
    public UserDetailDTO getUserByUsername(String username) {
        return MapperUtil.map(getUserEntityByUsername(username), UserDetailDTO.class);
    }

    @Override
    public User getUserEntityByUsername(String username) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new NotFoundException("User not found for username: " + username);
        }
        return user;
    }

    @Override
    @Transactional
    public UserDetailDTO updateUser(UserUpdateDTO updateData) {
        User existingUser = authenticationService.getCurrentUser();
        Log.infov("Updating user with username \"{0}\"", existingUser.getUsername());

        existingUser.setDisplayName(updateData.getDisplayName());
        existingUser.setBio(updateData.getBio());

        if(updateData.getProfilePicture() == null){
            Image oldProfilePicture = existingUser.getProfilePicture();
            existingUser.setProfilePicture(null);
            removeImage(oldProfilePicture);
        }

        if (updateData.getProfilePicture() != null && !updateData.getProfilePicture().isBlank() &&
                !(existingUser.getProfilePicture() != null && updateData.getProfilePicture().equals(existingUser.getProfilePicture().getId().toString()))) {
            setProfilePicture(updateData.getProfilePicture(), existingUser);
        }

        existingUser.setLocation(MapperUtil.map(updateData.getLocation(), UserLocation.class));

        // Persist the updated entity
        userRepository.persistUser(existingUser);

        return MapperUtil.map(existingUser, UserDetailDTO.class);
    }

    private void setProfilePicture(String profilePictureBase64, User existingUser) {
        Image oldProfilePicture = existingUser.getProfilePicture();

        Image newProfilePicture = Image.builder()
                .data(MapperUtil.decodeBase64(profilePictureBase64))
                .build();

        imageRepository.saveImage(newProfilePicture);

        existingUser.setProfilePicture(newProfilePicture);
        removeImage(oldProfilePicture);
    }

    private void removeImage(Image oldProfilePicture) {
        // remove old profile-picture if present, and only if it was only linked to one user
        if (oldProfilePicture != null && oldProfilePicture.getUsers().size() < 2) {
            imageRepository.removeImage(oldProfilePicture);
        }
    }

    @Override
    @Transactional
    public void deleteUser() {
        User user = authenticationService.getCurrentUser();
        Log.infov("Deleting user with username \"{0}\"", user.getUsername());

        boolean isDeleted = userRepository.deleteUser(user);
        if (!isDeleted) {
            throw new IllegalStateException("Failed to delete user with username:" + user.getUsername());
        }
    }

    @Override
    public UserDetailDTO getCurrentUser() {
        return MapperUtil.map(authenticationService.getCurrentUser(), UserDetailDTO.class);
    }
}
