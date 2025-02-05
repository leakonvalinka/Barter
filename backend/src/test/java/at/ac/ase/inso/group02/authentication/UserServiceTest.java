package at.ac.ase.inso.group02.authentication;

import at.ac.ase.inso.group02.authentication.dto.UserDetailDTO;
import at.ac.ase.inso.group02.authentication.dto.UserUpdateDTO;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.exceptions.UnauthenticatedException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@Slf4j
@QuarkusTest
public class UserServiceTest {
    @Inject
    UserService userService;

    @InjectMock
    UserRepository userRepositoryMock;

    @InjectMock
    RefreshTokenRepository tokenRepositoryMock;

    @InjectMock
    @InjectMocks
    AuthenticationService authenticationServiceMock;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        // Override the default behavior of tokenRepositoryMock for all test cases
        when(tokenRepositoryMock.saveNew(any())).thenReturn(true);
        when(tokenRepositoryMock.remove(any())).thenReturn(true);
    }

    @Test
    public void testGetUserByUsername_ShouldReturnUser() {
        // Arrange
        String username = "testUser";
        User user = User.builder()
                .username(username)
                .email("test@test.com")
                .build();
        when(userRepositoryMock.findByUsername(username)).thenReturn(user);

        // Act
        UserDetailDTO result = userService.getUserByUsername(username);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(username, result.getUsername());
        verify(userRepositoryMock, times(1)).findByUsername(username);
    }

    @Test
    public void testGetUserByUsername_ShouldThrowNotFoundException() {
        // Arrange
        String username = "nonexistentUser";
        when(userRepositoryMock.findByUsername(username)).thenReturn(null);

        // Act & Assert
        Assertions.assertThrows(NotFoundException.class, () -> userService.getUserByUsername(username));
        verify(userRepositoryMock, times(1)).findByUsername(username);
    }

    @Test
    public void testUpdateUser_ShouldUpdateDetails() {
        // Arrange
        String username = "testUser";
        User existingUser = User.builder()
                .username(username)
                .email("test@test.com")
                .build();
        UserUpdateDTO updateData = UserUpdateDTO.builder()
                .displayName("Updated Name")
                .bio("Updated Bio")
                .build();

        when(authenticationServiceMock.getCurrentUser()).thenReturn(existingUser);
        when(userRepositoryMock.findByUsername(username)).thenReturn(existingUser);

        // Act
        userService.updateUser(updateData);

        // Assert
        verify(userRepositoryMock, times(1)).persistUser(argThat(user ->
                user.getDisplayName().equals(updateData.getDisplayName()) &&
                        user.getBio().equals(updateData.getBio())
        ));
    }

    @Test
    public void testUpdateUser_ShouldThrowNotFoundException() {
        // Arrange
        String username = "nonexistentUser";
        UserUpdateDTO updateData = UserUpdateDTO.builder()
                .displayName("Updated Name")
                .build();

        when(userRepositoryMock.findByUsername(username)).thenReturn(null);
        when(authenticationServiceMock.getCurrentUsername()).thenReturn(username);
        when(authenticationServiceMock.getCurrentUser()).thenCallRealMethod();

        // Act & Assert
        Assertions.assertThrows(UnauthenticatedException.class, () -> userService.updateUser(updateData));
        verify(userRepositoryMock, times(0)).persistUser(any());
    }

    @Test
    public void testDeleteUser_ShouldDeleteSuccessfully() {
        // Arrange
        String username = "testUser";
        User user = User.builder()
                .username(username)
                .build();

        when(userRepositoryMock.findByUsername(username)).thenReturn(user);
        when(userRepositoryMock.deleteUser(user)).thenReturn(true);
        when(authenticationServiceMock.getCurrentUser()).thenReturn(user);

        // Act
        userService.deleteUser();

        // Assert
        verify(userRepositoryMock, times(1)).deleteUser(user);
    }

    @Test
    public void testDeleteUser_ShouldThrowNotFoundException() {
        // Arrange
        String username = "nonexistentUser";
        when(userRepositoryMock.findByUsername(username)).thenReturn(null);
        when(authenticationServiceMock.getCurrentUsername()).thenReturn(username);
        when(authenticationServiceMock.getCurrentUser()).thenCallRealMethod();

        // Act & Assert
        Assertions.assertThrows(UnauthenticatedException.class, () -> userService.deleteUser());
        verify(userRepositoryMock, times(0)).deleteUser(any());
    }
}
