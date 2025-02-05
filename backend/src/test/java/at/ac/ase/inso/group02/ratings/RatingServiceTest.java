package at.ac.ase.inso.group02.ratings;

import at.ac.ase.inso.group02.authentication.AuthenticationService;
import at.ac.ase.inso.group02.authentication.UserRepository;
import at.ac.ase.inso.group02.bartering.ExchangeRepository;
import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.entities.SkillOffer;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.exchange.ExchangeItem;
import at.ac.ase.inso.group02.entities.rating.InitiatorRating;
import at.ac.ase.inso.group02.entities.rating.ResponderRating;
import at.ac.ase.inso.group02.entities.rating.UserRating;
import at.ac.ase.inso.group02.exceptions.UnauthorizedCreationException;
import at.ac.ase.inso.group02.exceptions.UnauthorizedModificationException;
import at.ac.ase.inso.group02.rating.InitiatorRatingRepository;
import at.ac.ase.inso.group02.rating.RatingRepository;
import at.ac.ase.inso.group02.rating.RatingService;
import at.ac.ase.inso.group02.rating.ResponderRatingRepository;
import at.ac.ase.inso.group02.rating.dto.CreateRatingDTO;
import at.ac.ase.inso.group02.rating.dto.UserRatingDTO;
import at.ac.ase.inso.group02.rating.exception.RatingAlreadyExistsException;
import at.ac.ase.inso.group02.skills.GenericSkillRepository;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
public class RatingServiceTest {

    @Inject
    RatingService ratingService;

    @InjectMock
    RatingRepository ratingRepositoryMock;
    @InjectMock
    ExchangeRepository exchangeRepositoryMock;
    @InjectMock
    AuthenticationService authenticationServiceMock;
    @InjectMock
    ResponderRatingRepository responderRatingRepositoryMock;
    @InjectMock
    InitiatorRatingRepository initiatorRatingRepositoryMock;
    @InjectMock
    UserRepository userRepositoryMock;
    @InjectMock
    GenericSkillRepository<Skill> skillRepositoryMock;

    @Test
    void testCreateRating_shouldCreateNewRating() {
        // Arrange
        User byUser = User.builder().id(-1L).username("byUser").build();
        User forUser = User.builder().id(-2L).username("forUser").build();
        SkillOffer skill = SkillOffer.builder().byUser(byUser).build();
        ExchangeItem exchangeItem = ExchangeItem.builder()
                .id(-1L)
                .exchangedSkill(skill)
                .initiator(forUser)
                .numberOfExchanges(1)
                .ratable(true)
                .build();
        CreateRatingDTO newRating = CreateRatingDTO.builder()
                .ratingHalfStars(10)
                .title("Very sweet")
                .description("She was very sweet and super helpful")
                .build();

        // for updateRatingOfUser()
        PanacheQuery ratingQueryMock = mock(PanacheQuery.class);
        List<UserRating> ratings = List.of(InitiatorRating.builder().ratingHalfStars((short) 8).build(),
                ResponderRating.builder().ratingHalfStars((short) 8).build());
        when(ratingQueryMock.stream()).thenReturn(ratings.stream());
        when(ratingRepositoryMock.findRatingsForUser(any(User.class))).thenReturn(ratingQueryMock);
        when(userRepositoryMock.persistUser(any(User.class))).thenReturn(forUser);

        // for updateRatingOfSkill()
        PanacheQuery initiatorRatingQueryMock = mock(PanacheQuery.class);
        List<InitiatorRating> initiatorRatings = List.of(InitiatorRating.builder().ratingHalfStars((short) 8).build());
        when(initiatorRatingQueryMock.stream()).thenReturn(initiatorRatings.stream());
        when(initiatorRatingRepositoryMock.findRatingsForSkill(any(Skill.class))).thenReturn(initiatorRatingQueryMock);
        doNothing().when(skillRepositoryMock).persistAndFlush(any(Skill.class));

        when(exchangeRepositoryMock.findById(-1L)).thenReturn(exchangeItem);
        when(authenticationServiceMock.getCurrentUser()).thenReturn(byUser);
        when(responderRatingRepositoryMock.findByExchange(exchangeItem)).thenReturn(null);
        doNothing().when(ratingRepositoryMock).persistAndFlush(any(UserRating.class));

        // Act
        UserRatingDTO result = ratingService.createRatingForExchange(-1L, newRating);

        // Assert
        assertEquals(newRating.getTitle(), result.getTitle());
        assertEquals(newRating.getDescription(), result.getDescription());
        assertEquals(newRating.getRatingHalfStars(), result.getRatingHalfStars());
        verify(ratingRepositoryMock, times(1)).persistAndFlush(any(UserRating.class));
    }

    @Test
    void testCreateRating_shouldFailForAlreadyExistentRating() {
        // Arrange
        User byUser = User.builder().id(-1L).username("byUser").build();
        User forUser = User.builder().id(-2L).username("forUser").build();
        SkillOffer skill = SkillOffer.builder().byUser(byUser).build();
        ExchangeItem exchange = ExchangeItem.builder()
                .id(-1L)
                .exchangedSkill(skill)
                .initiator(forUser)
                .numberOfExchanges(1)
                .ratable(true)
                .build();
        CreateRatingDTO newRating = CreateRatingDTO.builder()
                .ratingHalfStars(10)
                .title("Very sweet")
                .description("She was very sweet and super helpful")
                .build();

        when(exchangeRepositoryMock.findById(-1L)).thenReturn(exchange);
        when(authenticationServiceMock.getCurrentUser()).thenReturn(byUser);
        when(responderRatingRepositoryMock.findByExchange(exchange)).thenReturn(ResponderRating.builder().build());

        // Act & Assert
        Exception exception = assertThrows(RatingAlreadyExistsException.class,
                () -> ratingService.createRatingForExchange(-1L, newRating));
        assertEquals("You already left a rating for this exchange!", exception.getMessage());
        verify(ratingRepositoryMock, never()).persistAndFlush(any(UserRating.class));
    }

    @Test
    void testCreateRating_shouldFailForMissingExchange() {
        // Arrange
        when(exchangeRepositoryMock.findById(-1L)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(NotFoundException.class,
                () -> ratingService.createRatingForExchange(-1L, CreateRatingDTO.builder().build()));
        assertEquals("No exchange found with id -1", exception.getMessage());
        verify(ratingRepositoryMock, never()).persistAndFlush(any(UserRating.class));
    }

    @Test
    void testCreateRating_shouldFailForUnauthorizedUser() {
        // Arrange
        User byUser = User.builder().id(-1L).username("byUser").build();
        User forUser = User.builder().id(-2L).username("forUser").build();
        User randomUser = User.builder().id(-3L).username("randomUser").build();
        SkillOffer skill = SkillOffer.builder().byUser(byUser).build();
        ExchangeItem exchangeItem = ExchangeItem.builder()
                .id(-1L)
                .exchangedSkill(skill)
                .initiator(forUser)
                .numberOfExchanges(1)
                .ratable(true)
                .build();

        when(exchangeRepositoryMock.findById(-1L)).thenReturn(exchangeItem);
        when(authenticationServiceMock.getCurrentUser()).thenReturn(randomUser);

        // Act & Assert
        Exception exception = assertThrows(UnauthorizedCreationException.class,
                () -> ratingService.createRatingForExchange(-1L, CreateRatingDTO.builder().build()));

        assertEquals("You are not allowed to create a rating for the exchange, since you were not part of it",
                exception.getMessage());
        verify(ratingRepositoryMock, never()).persistAndFlush(any(UserRating.class));
    }

    @Test
    void testGetUserRating_shouldFindTwoRatings() {
        // Arrange
        User byUser = User.builder().id(-1L).username("byUser").build();
        User forUser = User.builder().id(-2L).username("forUser").build();
        SkillOffer skill = SkillOffer.builder().byUser(byUser).build();
        ExchangeItem exchangeItem = ExchangeItem.builder()
                .id(-1L)
                .exchangedSkill(skill)
                .initiator(forUser)
                .numberOfExchanges(1)
                .ratable(true)
                .build();

        PanacheQuery ratingQueryMock = mock(PanacheQuery.class);
        List<UserRating> ratings = List.of(
                InitiatorRating.builder()
                        .title("Very sweet")
                        .description("She was very sweet and super helpful")
                        .ratingHalfStars((short) 10)
                        .initiatorExchange(exchangeItem)
                        .build(),
                ResponderRating.builder()
                        .title("Helpful")
                        .description("He was helpful and nice, but 15 minutes late")
                        .ratingHalfStars((short) 8)
                        .responderExchange(exchangeItem)
                        .build()
        );

        when(ratingQueryMock.page(any())).thenReturn(ratingQueryMock);
        when(ratingQueryMock.page()).thenReturn(new Page(0, 50));
        when(ratingQueryMock.count()).thenReturn((long) ratings.size());
        when(ratingQueryMock.pageCount()).thenReturn(1);
        when(ratingQueryMock.stream()).thenReturn(ratings.stream());
        when(ratingRepositoryMock.findRatingsForUser(any(User.class))).thenReturn(ratingQueryMock);
        when(userRepositoryMock.findByUsername("forUser")).thenReturn(forUser);

        // Act
        PaginatedQueryDTO<UserRatingDTO> result = ratingService.getUserRatings("forUser", new PaginationParamsDTO());

        // Assert
        assertEquals(2, result.getTotal());
        assertTrue(result.getItems().stream().anyMatch(rating -> rating.getRatingHalfStars() == 8));
        assertTrue(result.getItems().stream().anyMatch(rating -> rating.getRatingHalfStars() == 10));
        verify(ratingRepositoryMock, times(1)).findRatingsForUser(any(User.class));
    }

    @Test
    void testGetUserRating_shouldFindZeroRatings() {
        // Arrange
        User byUser = User.builder().id(-1L).username("byUser").build();

        PanacheQuery ratingQueryMock = mock(PanacheQuery.class);
        List<UserRating> ratings = List.of();

        when(ratingQueryMock.page(any())).thenReturn(ratingQueryMock);
        when(ratingQueryMock.page()).thenReturn(new Page(0, 50));
        when(ratingQueryMock.count()).thenReturn(((long) 0));
        when(ratingQueryMock.pageCount()).thenReturn(1);
        when(ratingQueryMock.stream()).thenReturn(ratings.stream());
        when(ratingRepositoryMock.findRatingsForUser(any(User.class))).thenReturn(ratingQueryMock);
        when(userRepositoryMock.findByUsername("byUser")).thenReturn(byUser);

        // Act
        PaginatedQueryDTO<UserRatingDTO> result = ratingService.getUserRatings("byUser", new PaginationParamsDTO());

        // Assert
        assertEquals(0, result.getTotal());
        assertTrue(result.getItems().isEmpty());
        verify(ratingRepositoryMock, times(1)).findRatingsForUser(any(User.class));
    }

    @Test
    void testGetUserRating_shouldFailForNonExistentUser() {
        // Arrange
        when(userRepositoryMock.findByUsername("noUser")).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(NotFoundException.class,
                () -> ratingService.getUserRatings("noUser", new PaginationParamsDTO()));

        assertEquals("No user found with username noUser", exception.getMessage());
        verify(ratingRepositoryMock, never()).findRatingsForUser(any(User.class));
    }

    @Test
    void testGetSkillRating_shouldFindOneRating() {
        // Arrange
        User byUser = User.builder().id(-1L).username("byUser").build();
        User forUser = User.builder().id(-2L).username("forUser").build();
        SkillOffer skill = SkillOffer.builder().byUser(byUser).build();
        ExchangeItem exchangeItem = ExchangeItem.builder()
                .id(-1L)
                .exchangedSkill(skill)
                .initiator(forUser)
                .numberOfExchanges(1)
                .ratable(true)
                .build();

        PanacheQuery ratingQueryMock = mock(PanacheQuery.class);
        List<InitiatorRating> ratings = List.of(
                InitiatorRating.builder()
                        .title("Very sweet")
                        .description("She was very sweet and super helpful")
                        .ratingHalfStars((short) 10)
                        .initiatorExchange(exchangeItem)
                        .build());

        when(initiatorRatingRepositoryMock.findRatingsForSkill(any(Skill.class))).thenReturn(ratingQueryMock);
        when(ratingQueryMock.page(any())).thenReturn(ratingQueryMock);
        when(ratingQueryMock.page()).thenReturn(new Page(0, 50));
        when(ratingQueryMock.count()).thenReturn((long) ratings.size());
        when(ratingQueryMock.pageCount()).thenReturn(1);
        when(ratingQueryMock.stream()).thenReturn(ratings.stream());

        // Act
        PaginatedQueryDTO<UserRatingDTO> result = ratingService.getSkillRatings(skill, new PaginationParamsDTO());

        // Assert
        assertEquals(1, result.getTotal());
        assertTrue(result.getItems().stream().anyMatch(rating -> rating.getRatingHalfStars() == 10));
        verify(initiatorRatingRepositoryMock, times(1)).findRatingsForSkill(any(Skill.class));
    }

    @Test
    void testDeleteRating_shouldDeleteRating() {
        // Arrange
        User byUser = User.builder().id(-1L).username("byUser").build();
        User forUser = User.builder().id(-2L).username("forUser").build();
        SkillOffer skill = SkillOffer.builder().byUser(forUser).build();
        ExchangeItem exchangeItem = ExchangeItem.builder()
                .id(-1L)
                .exchangedSkill(skill)
                .initiator(byUser)
                .numberOfExchanges(1)
                .ratable(true)
                .build();
        InitiatorRating rating = InitiatorRating.builder()
                .id(-1L)
                .initiatorExchange(exchangeItem)
                .build();

        // for updateRatingOfUser
        PanacheQuery ratingQueryMock = mock(PanacheQuery.class);
        List<UserRating> ratings = List.of(InitiatorRating.builder().ratingHalfStars((short) 8).build(),
                ResponderRating.builder().ratingHalfStars((short) 8).build());
        when(ratingQueryMock.stream()).thenReturn(ratings.stream());
        when(ratingRepositoryMock.findRatingsForUser(any(User.class))).thenReturn(ratingQueryMock);
        when(userRepositoryMock.persistUser(any(User.class))).thenReturn(forUser);

        // for updateRatingOfSkill
        PanacheQuery initiatorRatingQueryMock = mock(PanacheQuery.class);
        List<InitiatorRating> initiatorRatings = List.of(InitiatorRating.builder().ratingHalfStars((short) 8).build());
        when(initiatorRatingQueryMock.stream()).thenReturn(initiatorRatings.stream());
        when(initiatorRatingRepositoryMock.findRatingsForSkill(any(Skill.class))).thenReturn(initiatorRatingQueryMock);
        doNothing().when(skillRepositoryMock).persistAndFlush(any(Skill.class));

        when(ratingRepositoryMock.findById(-1L)).thenReturn(rating);
        when(authenticationServiceMock.getCurrentUser()).thenReturn(byUser);
        doNothing().when(ratingRepositoryMock).delete(rating);
        doNothing().when(ratingRepositoryMock).flush();

        // Act & Assert
        assertTrue(ratingService.deleteRating(-1L));
        verify(ratingRepositoryMock, times(1)).delete(rating);
    }

    @Test
    void testDeleteRating_shouldFailForNonExistentRating() {
        // Arrange
        when(ratingRepositoryMock.findById(-1L)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(NotFoundException.class, () -> ratingService.deleteRating(-1L));
        assertEquals("No such rating found with id -1", exception.getMessage());
    }

    @Test
    void testDeleteRating_shouldFailWhenDeletingRatingOfOtherUser() {
        // Arrange
        User byUser = User.builder().id(-1L).username("byUser").build();
        User forUser = User.builder().id(-2L).username("forUser").build();
        User randomUser = User.builder().id(-3L).username("randomUser").build();
        ExchangeItem exchangeItem = ExchangeItem.builder()
                .id(-1L)
                .exchangedSkill(SkillOffer.builder().byUser(forUser).build())
                .initiator(byUser)
                .ratable(true)
                .build();
        InitiatorRating rating = InitiatorRating.builder()
                .id(-1L)
                .initiatorExchange(exchangeItem)
                .build();

        when(ratingRepositoryMock.findById(-1L)).thenReturn(rating);
        when(authenticationServiceMock.getCurrentUser()).thenReturn(randomUser);

        // Act & Assert
        Exception exception = assertThrows(UnauthorizedModificationException.class,
                () -> ratingService.deleteRating(-1L));
        assertEquals("You are not allowed to delete this rating", exception.getMessage());
    }

    @Test
    void testUpdateRating_shouldSuccessfullyUpdateRating() {
        // Arrange
        User byUser = User.builder().id(-1L).username("byUser").build();
        User forUser = User.builder().id(-2L).username("forUser").build();
        ExchangeItem exchangeItem = ExchangeItem.builder()
                .id(-1L)
                .exchangedSkill(SkillOffer.builder().byUser(forUser).build())
                .initiator(byUser)
                .numberOfExchanges(1)
                .ratable(true)
                .build();
        InitiatorRating oldRating = InitiatorRating.builder()
                .ratingHalfStars((short) 9)
                .title("Nice")
                .description("She was helpful and friendly")
                .initiatorExchange(exchangeItem)
                .build();
        CreateRatingDTO updateRating = CreateRatingDTO.builder()
                .ratingHalfStars(10)
                .title("Very sweet")
                .description("She was very sweet and super helpful")
                .build();

        // for updateRatingOfUser()
        PanacheQuery ratingQueryMock = mock(PanacheQuery.class);
        List<UserRating> ratings = List.of(InitiatorRating.builder().ratingHalfStars((short) 8).build(),
                ResponderRating.builder().ratingHalfStars((short) 8).build());
        when(ratingQueryMock.stream()).thenReturn(ratings.stream());
        when(ratingRepositoryMock.findRatingsForUser(any(User.class))).thenReturn(ratingQueryMock);
        when(userRepositoryMock.persistUser(any(User.class))).thenReturn(forUser);

        // for updateRatingOfSkill()
        PanacheQuery initiatorRatingQueryMock = mock(PanacheQuery.class);
        List<InitiatorRating> initiatorRatings = List.of(InitiatorRating.builder().ratingHalfStars((short) 8).build());
        when(initiatorRatingQueryMock.stream()).thenReturn(initiatorRatings.stream());
        when(initiatorRatingRepositoryMock.findRatingsForSkill(any(Skill.class))).thenReturn(initiatorRatingQueryMock);
        doNothing().when(skillRepositoryMock).persistAndFlush(any(Skill.class));

        when(ratingRepositoryMock.findById(-1L)).thenReturn(oldRating);
        when(authenticationServiceMock.getCurrentUser()).thenReturn(byUser);
        doNothing().when(ratingRepositoryMock).persistAndFlush(any(UserRating.class));

        // Act
        UserRatingDTO result = ratingService.updateRating(-1L, updateRating);

        // Assert
        assertEquals(updateRating.getTitle(), result.getTitle());
        assertEquals(updateRating.getDescription(), result.getDescription());
        assertEquals(updateRating.getRatingHalfStars(), result.getRatingHalfStars());
        verify(ratingRepositoryMock, times(1)).persistAndFlush(any(UserRating.class));
    }

    @Test
    void testUpdateRating_shouldFailWhenUpdatingRatingOfOtherUser() {
        // Arrange
        User byUser = User.builder().id(-1L).username("byUser").build();
        User forUser = User.builder().id(-2L).username("forUser").build();
        User randomUser = User.builder().id(-3L).username("randomUser").build();
        ExchangeItem exchangeItem = ExchangeItem.builder()
                .id(-1L)
                .exchangedSkill(SkillOffer.builder().byUser(forUser).build())
                .initiator(byUser)
                .ratable(true)
                .build();
        InitiatorRating oldRating = InitiatorRating.builder()
                .ratingHalfStars((short) 9)
                .title("Nice")
                .description("She was helpful and friendly")
                .initiatorExchange(exchangeItem)
                .build();
        CreateRatingDTO updateRating = CreateRatingDTO.builder()
                .ratingHalfStars(10)
                .title("Very sweet")
                .description("She was very sweet and super helpful")
                .build();

        when(ratingRepositoryMock.findById(-1L)).thenReturn(oldRating);
        when(authenticationServiceMock.getCurrentUser()).thenReturn(randomUser);

        // Act & Assert
        Exception exception = assertThrows(UnauthorizedModificationException.class,
                () -> ratingService.updateRating(-1L, updateRating));
        assertEquals("You are not allowed to update this rating", exception.getMessage());
    }

    @Test
    void testUpdateRating_shouldFailForNonExistentRating() {
        // Arrange
        when(ratingRepositoryMock.findById(-1L)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(NotFoundException.class,
                () -> ratingService.updateRating(-1L, CreateRatingDTO.builder().build()));
        assertEquals("No such rating found with id -1", exception.getMessage());
    }
}
