package at.ac.ase.inso.group02.skills;

import at.ac.ase.inso.group02.authentication.AuthenticationService;
import at.ac.ase.inso.group02.entities.*;
import at.ac.ase.inso.group02.exceptions.UnauthenticatedException;
import at.ac.ase.inso.group02.exceptions.UnauthorizedModificationException;
import at.ac.ase.inso.group02.skills.dto.*;
import at.ac.ase.inso.group02.skills.exception.SkillCategoryDoesNotExistException;
import at.ac.ase.inso.group02.skills.exception.SkillDoesNotExistException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
@QuarkusTest
public class SkillServiceTest {
    @Inject
    SkillDemandService skillDemandService;

    @Inject
    SkillOfferService skillOfferService;

    @Inject
    SkillService skillService;

    @InjectMock
    GenericSkillRepository<Skill> skillRepository;

    @InjectMock
    GenericSkillRepository<SkillOffer> skillOfferRepository;

    @InjectMock
    GenericSkillRepository<SkillDemand> skillDemandRepository;

    static User testUser = User.builder()
            .roles(Collections.singleton(UserRole.builder().role("USER").build()))
            .id(-100L)
            .username("test")
            .build();

    @InjectMock
    AuthenticationService authenticationServiceMock;

    @InjectMock
    @InjectMocks
    SkillCategoryRepository skillCategoryRepository;

    @BeforeEach
    public void setup() {
        when(authenticationServiceMock.getCurrentUser()).thenReturn(testUser);
    }

    private static void assertSkillDemandEquals(CreateSkillDemandDTO createSkillDTO, SkillDemandDTO skill) {
        Assertions.assertEquals("demand", skill.getType());
        Assertions.assertEquals(createSkillDTO.getTitle(), skill.getTitle());
        Assertions.assertEquals(createSkillDTO.getDescription(), skill.getDescription());
        Assertions.assertEquals(createSkillDTO.getCategory().getId(), skill.getCategory().getId());
        Assertions.assertEquals(createSkillDTO.getUrgency(), skill.getUrgency());
        Assertions.assertEquals(testUser.getUsername(), skill.getByUser().getUsername());
    }

    private static void assertSkillOfferEquals(CreateSkillOfferDTO createSkillDTO, SkillOfferDTO skill) {
        Assertions.assertEquals("offer", skill.getType());
        Assertions.assertEquals(createSkillDTO.getTitle(), skill.getTitle());
        Assertions.assertEquals(createSkillDTO.getDescription(), skill.getDescription());
        Assertions.assertEquals(createSkillDTO.getCategory().getId(), skill.getCategory().getId());
        Assertions.assertEquals(createSkillDTO.getSchedule(), skill.getSchedule());
        Assertions.assertEquals(testUser.getUsername(), skill.getByUser().getUsername());
    }

    static Stream<CreateSkillDTO> getCreateSkillDemandDTOs() {
        // Arrange
        return Stream.of(
                CreateSkillDemandDTO.builder()
                        .title("Demand 1")
                        .description("I need this, and quick!")
                        .urgency(DemandUrgency.HIGH)
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder()
                                .id(1L)
                                .build())
                        .build(),
                CreateSkillDemandDTO.builder()
                        .title("Demand 2")
                        .description("I also need this, but it can wait!")
                        .urgency(DemandUrgency.LOW)
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder()
                                .id(2L)
                                .build())
                        .build(),
                CreateSkillDemandDTO.builder()
                        .title("Demand 3")
                        .description("PLEASE HELP IMMEDIATELY AHHHHHH!")
                        .urgency(DemandUrgency.CRITICAL)
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder()
                                .id(3L)
                                .build())
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource("getCreateSkillDemandDTOs")
    public void testCreateSkillDemand_shouldSucceedForValidInput(CreateSkillDemandDTO createSkillDTO) {
        // Arrange
        when(skillCategoryRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return SkillCategory.builder()
                    .id(id)
                    .description("Blabla")
                    .name("Bla")
                    .build();
        });

        // Act
        SkillDemandDTO skill = skillDemandService.createSkill(createSkillDTO);

        // Assert
        assertSkillDemandEquals(createSkillDTO, skill);
        verify(skillDemandRepository).persistAndFlush(
                argThat(s ->
                        s.getUrgency() == createSkillDTO.getUrgency()
                                && s.getByUser().equals(testUser)
                                && s.getCategory().getId().equals(createSkillDTO.getCategory().getId())
                                && s.getTitle().equals(createSkillDTO.getTitle())
                                && s.getDescription().equals(createSkillDTO.getDescription())
                )
        );
    }


    static Stream<CreateSkillDTO> getCreateSkillOfferDTOs() {
        // Arrange
        return Stream.of(
                CreateSkillOfferDTO.builder()
                        .title("ASE VU")
                        .description("I teach people about software verification.")
                        .schedule("Every Thursday, 13-14h")
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder()
                                .id(1L)
                                .build())
                        .build(),
                CreateSkillOfferDTO.builder()
                        .title("Offer 2")
                        .description("I offer this")
                        .schedule("Whenever")
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder()
                                .id(2L)
                                .build())
                        .build(),
                CreateSkillOfferDTO.builder()
                        .title("Offer 3")
                        .description("I offer that")
                        .schedule("Weekends")
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder()
                                .id(3L)
                                .build())
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource("getCreateSkillOfferDTOs")
    public void testCreateSkillOffer_shouldSucceedForValidInput(CreateSkillOfferDTO createSkillDTO) {
        // Arrange
        when(skillCategoryRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return SkillCategory.builder()
                    .id(id)
                    .description("Blabla")
                    .name("Bla")
                    .build();
        });

        // Act
        SkillOfferDTO skill = skillOfferService.createSkill(createSkillDTO);

        // Assert
        assertSkillOfferEquals(createSkillDTO, skill);
        verify(skillOfferRepository).persistAndFlush(
                argThat(s ->
                        Objects.equals(s.getSchedule(), createSkillDTO.getSchedule())
                                && s.getByUser().equals(testUser)
                                && s.getCategory().getId().equals(createSkillDTO.getCategory().getId())
                                && s.getTitle().equals(createSkillDTO.getTitle())
                                && s.getDescription().equals(createSkillDTO.getDescription())
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getCreateSkillOfferDTOs")
    public void testCreateInvalidCategoryOffer_shouldFail(CreateSkillOfferDTO createSkillOfferDTO) {
        // Arrange
        when(skillCategoryRepository.findById(anyLong())).thenReturn(null);

        // Act & Assert
        Assertions.assertThrows(SkillCategoryDoesNotExistException.class, () -> skillOfferService.createSkill(createSkillOfferDTO));
        // nothing should be persisted
        verify(skillOfferRepository, times(0)).persistAndFlush(any());
    }

    @ParameterizedTest
    @MethodSource("getCreateSkillDemandDTOs")
    public void testCreateInvalidCategoryDemand_shouldFail(CreateSkillDemandDTO createSkillDemandDTO) {
        // Arrange
        when(skillCategoryRepository.findById(anyLong())).thenReturn(null);

        // Act & Assert
        Assertions.assertThrows(SkillCategoryDoesNotExistException.class, () -> skillDemandService.createSkill(createSkillDemandDTO));
        // nothing should be persisted
        verify(skillDemandRepository, times(0)).persistAndFlush(any());
    }

    static Stream<CreateSkillDTO> getInvalidCreateSkillOfferDTOs() {
        // Arrange
        return Stream.of(
                CreateSkillOfferDTO.builder()
                        .title("E")
                        .description("The title of this offer is clearly too short!")
                        .schedule("Whenever")
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder()
                                .id(1L)
                                .build())
                        .build(),
                CreateSkillOfferDTO.builder()
                        .title("I couldn't decide which category to put, so I put none")
                        .description("What the title says")
                        .schedule("Whenever")
                        .category(null)
                        .build(),
                CreateSkillOfferDTO.builder()
                        .title("I also couldn't decide which category to put, so I put none")
                        .description("What the title says")
                        .schedule("Whenever")
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder()
                                .id(null)
                                .build())
                        .build(),
                CreateSkillOfferDTO.builder()
                        .title("Title, but no description")
                        .description("")
                        .schedule("Whenever")
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder()
                                .id(1L)
                                .build())
                        .build(),
                CreateSkillOfferDTO.builder()
                        .title("Title, but no description")
                        .description(null)
                        .schedule("Whenever")
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder()
                                .id(1L)
                                .build())
                        .build(),
                CreateSkillOfferDTO.builder()
                        .title("")
                        .description("Description, but no title")
                        .schedule("Whenever")
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder()
                                .id(1L)
                                .build())
                        .build(),
                CreateSkillOfferDTO.builder()
                        .title(null)
                        .description("Description, but no title")
                        .schedule("Whenever")
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder()
                                .id(1L)
                                .build())
                        .build(),
                CreateSkillOfferDTO.builder()
                        .title("No Schedule")
                        .description("No Schedule lol")
                        .schedule("")
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder()
                                .id(1L)
                                .build())
                        .build(),
                CreateSkillOfferDTO.builder()
                        .title("No Schedule")
                        .description("No Schedule lol")
                        .schedule(null)
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder()
                                .id(1L)
                                .build())
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource("getInvalidCreateSkillOfferDTOs")
    public void testInvalidCreateSkillOffer_shouldFail(CreateSkillOfferDTO createSkillOfferDTO) {
        // Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class, () -> skillOfferService.createSkill(createSkillOfferDTO));
        // nothing should be persisted
        verify(skillOfferRepository, times(0)).persistAndFlush(any());
    }

    static Stream<CreateSkillDTO> getInvalidCreateSkillDemandDTOs() {
        // Arrange
        return Stream.of(
                CreateSkillDemandDTO.builder()
                        .title("E")
                        .description("The title of this offer is clearly too short!")
                        .urgency(DemandUrgency.LOW)
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder()
                                .id(1L)
                                .build())
                        .build(),
                CreateSkillDemandDTO.builder()
                        .title("I couldn't decide which category to put, so I put none")
                        .description("What the title says")
                        .urgency(DemandUrgency.LOW)
                        .category(null)
                        .build(),
                CreateSkillDemandDTO.builder()
                        .title("I also couldn't decide which category to put, so I put none")
                        .description("What the title says")
                        .urgency(DemandUrgency.LOW)
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder()
                                .id(null)
                                .build())
                        .build(),
                CreateSkillDemandDTO.builder()
                        .title("Title, but no description")
                        .description("")
                        .urgency(DemandUrgency.LOW)
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder()
                                .id(1L)
                                .build())
                        .build(),
                CreateSkillDemandDTO.builder()
                        .title("Title, but no description")
                        .description(null)
                        .urgency(DemandUrgency.LOW)
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder()
                                .id(1L)
                                .build())
                        .build(),
                CreateSkillDemandDTO.builder()
                        .title("")
                        .description("Description, but no title")
                        .urgency(DemandUrgency.LOW)
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder()
                                .id(1L)
                                .build())
                        .build(),
                CreateSkillDemandDTO.builder()
                        .title(null)
                        .description("Description, but no title")
                        .urgency(DemandUrgency.LOW)
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder()
                                .id(1L)
                                .build())
                        .build(),
                CreateSkillDemandDTO.builder()
                        .title("No Schedule")
                        .description("No Urgency lol")
                        .urgency(null)
                        .category(CreateSkillDTO.SkillCreateCategoryDTO.builder()
                                .id(1L)
                                .build())
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource("getInvalidCreateSkillDemandDTOs")
    public void testInvalidCreateSkillDemand_shouldFail(CreateSkillDemandDTO createSkillDemandDTO) {
        // Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class, () -> skillDemandService.createSkill(createSkillDemandDTO));
        // nothing should be persisted
        verify(skillDemandRepository, times(0)).persistAndFlush(any());
    }


    @ParameterizedTest
    @MethodSource("getCreateSkillOfferDTOs")
    public void testCreateOfferUnauthenticated_shouldFail(CreateSkillOfferDTO createSkillOfferDTO) {
        // Arrange
        when(authenticationServiceMock.getCurrentUser()).thenThrow(new UnauthenticatedException("Unauthenticated"));

        // Act & Assert
        Assertions.assertThrows(UnauthenticatedException.class, () -> skillOfferService.createSkill(createSkillOfferDTO));
        // nothing should be persisted
        verify(skillOfferRepository, times(0)).persistAndFlush(any());
    }

    @ParameterizedTest
    @MethodSource("getCreateSkillDemandDTOs")
    public void testCreateDemandUnauthenticated_shouldFail(CreateSkillDemandDTO createSkillDemandDTO) {
        // Arrange
        when(authenticationServiceMock.getCurrentUser()).thenThrow(new UnauthenticatedException("Unauthenticated"));

        // Act & Assert
        Assertions.assertThrows(UnauthenticatedException.class, () -> skillDemandService.createSkill(createSkillDemandDTO));
        // nothing should be persisted
        verify(skillDemandRepository, times(0)).persistAndFlush(any());
    }


    @ParameterizedTest
    @MethodSource("getCreateSkillDemandDTOs")
    public void testUpdateSkillDemand_shouldSucceedForValidInput(CreateSkillDemandDTO updateSkillDTO) {
        // Arrange
        when(skillCategoryRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return SkillCategory.builder()
                    .id(id)
                    .description("Blabla")
                    .name("Bla")
                    .build();
        });

        long id = 1L;

        when(skillDemandRepository.findById(id)).thenReturn(SkillDemand.builder()
                .urgency(DemandUrgency.LOW)
                .title("Some existing Demand")
                .id(id)
                .category(SkillCategory.builder()
                        .id(1000L)
                        .description("Not Blabla")
                        .name("Not Bla")
                        .build())
                .description("This demand already exists")
                .byUser(testUser)
                .build());

        // Act
        SkillDemandDTO skill = skillDemandService.updateSkill(id, updateSkillDTO);

        // Assert
        assertSkillDemandEquals(updateSkillDTO, skill);
        verify(skillDemandRepository).persistAndFlush(
                argThat(s ->
                        s.getUrgency() == updateSkillDTO.getUrgency()
                                && s.getByUser().equals(testUser)
                                && s.getCategory().getId().equals(updateSkillDTO.getCategory().getId())
                                && s.getTitle().equals(updateSkillDTO.getTitle())
                                && s.getDescription().equals(updateSkillDTO.getDescription())
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getCreateSkillOfferDTOs")
    public void testUpdateSkillOffer_shouldSucceedForValidInput(CreateSkillOfferDTO updateSkillDTO) {
        // Arrange
        when(skillCategoryRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return SkillCategory.builder()
                    .id(id)
                    .description("Blabla")
                    .name("Bla")
                    .build();
        });

        long id = 1L;

        when(skillOfferRepository.findById(id)).thenReturn(SkillOffer.builder()
                .schedule("Some preexisting schedule")
                .title("Some existing Demand")
                .id(id)
                .category(SkillCategory.builder()
                        .id(1000L)
                        .description("Not Blabla")
                        .name("Not Bla")
                        .build())
                .description("This demand already exists")
                .byUser(testUser)
                .build());

        // Act
        SkillOfferDTO skill = skillOfferService.updateSkill(id, updateSkillDTO);

        // Assert
        assertSkillOfferEquals(updateSkillDTO, skill);
        verify(skillOfferRepository).persistAndFlush(
                argThat(s ->
                        Objects.equals(s.getSchedule(), updateSkillDTO.getSchedule())
                                && s.getByUser().equals(testUser)
                                && s.getCategory().getId().equals(updateSkillDTO.getCategory().getId())
                                && s.getTitle().equals(updateSkillDTO.getTitle())
                                && s.getDescription().equals(updateSkillDTO.getDescription())
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getCreateSkillDemandDTOs")
    public void testUpdateSkillDemandIncorrectUser_shouldFail(CreateSkillDemandDTO updateSkillDTO) {
        // Arrange
        when(skillCategoryRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return SkillCategory.builder()
                    .id(id)
                    .description("Blabla")
                    .name("Bla")
                    .build();
        });

        long id = 1L;

        when(skillDemandRepository.findById(id)).thenReturn(SkillDemand.builder()
                .urgency(DemandUrgency.LOW)
                .title("Some existing Demand")
                .id(id)
                .category(SkillCategory.builder()
                        .id(1000L)
                        .description("Not Blabla")
                        .name("Not Bla")
                        .build())
                .description("This demand already exists")
                .byUser(User.builder()
                        .email("not.thetest.user@user.com")
                        .username("other_user")
                        .build())
                .build());

        // Act & Assert
        Assertions.assertThrows(UnauthorizedModificationException.class, () -> skillDemandService.updateSkill(id, updateSkillDTO));
        verify(skillDemandRepository, times(0)).persistAndFlush(any());
    }

    @ParameterizedTest
    @MethodSource("getCreateSkillOfferDTOs")
    public void testUpdateSkillOfferIncorrectUser_shouldFail(CreateSkillOfferDTO updateSkillDTO) {
        // Arrange
        when(skillCategoryRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return SkillCategory.builder()
                    .id(id)
                    .description("Blabla")
                    .name("Bla")
                    .build();
        });

        long id = 1L;

        when(skillOfferRepository.findById(id)).thenReturn(SkillOffer.builder()
                .schedule("Some preexisting schedule")
                .title("Some existing Demand")
                .id(id)
                .category(SkillCategory.builder()
                        .id(1000L)
                        .description("Not Blabla")
                        .name("Not Bla")
                        .build())
                .description("This demand already exists")
                .byUser(User.builder()
                        .email("not.thetest.user@user.com")
                        .username("other_user")
                        .build())
                .build());

        // Act & Assert
        Assertions.assertThrows(UnauthorizedModificationException.class, () -> skillOfferService.updateSkill(id, updateSkillDTO));
        verify(skillOfferRepository, times(0)).persistAndFlush(any());
    }


    @Test
    public void testDeleteSkillOffer_shouldSucceedForValidInput() {
        // Arrange
        long id = 1L;

        SkillOffer skillOffer = SkillOffer.builder()
                .schedule("Some preexisting schedule")
                .title("Some existing Demand")
                .category(SkillCategory.builder()
                        .id(1000L)
                        .description("Not Blabla")
                        .name("Not Bla")
                        .build())
                .description("This demand already exists")
                .byUser(testUser)
                .id(id)
                .build();

        when(skillOfferRepository.findById(id)).thenReturn(skillOffer);

        // Act
        skillOfferService.deleteSkillById(id);

        // Assert
        verify(skillOfferRepository).delete(skillOffer);
    }

    @Test
    public void testDeleteSkillDemand_shouldSucceedForValidInput() {
        // Arrange
        long id = 1L;

        SkillDemand skillDemand = SkillDemand.builder()
                .urgency(DemandUrgency.LOW)
                .title("Some existing Demand")
                .category(SkillCategory.builder()
                        .id(1000L)
                        .description("Not Blabla")
                        .name("Not Bla")
                        .build())
                .description("This demand already exists")
                .byUser(testUser)
                .id(id)
                .build();

        when(skillDemandRepository.findById(id)).thenReturn(skillDemand);

        // Act
        skillDemandService.deleteSkillById(id);

        // Assert
        verify(skillDemandRepository).delete(skillDemand);
    }

    @Test
    public void testDeleteSkillOfferIncorrectUser_shouldFail() {
        // Arrange
        long id = 1L;

        SkillOffer skillOffer = SkillOffer.builder()
                .schedule("Some preexisting schedule")
                .title("Some existing Demand")
                .category(SkillCategory.builder()
                        .id(1000L)
                        .description("Not Blabla")
                        .name("Not Bla")
                        .build())
                .description("This demand already exists")
                .byUser(User.builder()
                        .email("not.thetest.user@user.com")
                        .username("other_user")
                        .build())
                .id(id)
                .build();

        when(skillOfferRepository.findById(id)).thenReturn(skillOffer);

        // Act & Assert
        Assertions.assertThrows(UnauthorizedModificationException.class, () -> skillOfferService.deleteSkillById(id));
        verify(skillOfferRepository, times(0)).delete(any());
    }

    @Test
    public void testDeleteSkillDemandIncorrectUser_shouldFail() {
        // Arrange
        long id = 1L;

        SkillDemand skillDemand = SkillDemand.builder()
                .urgency(DemandUrgency.LOW)
                .title("Some existing Demand")
                .category(SkillCategory.builder()
                        .id(1000L)
                        .description("Not Blabla")
                        .name("Not Bla")
                        .build())
                .description("This demand already exists")
                .byUser(User.builder()
                        .email("not.thetest.user@user.com")
                        .username("other_user")
                        .build())
                .id(id)
                .build();

        when(skillDemandRepository.findById(id)).thenReturn(skillDemand);

        // Act & Assert
        Assertions.assertThrows(UnauthorizedModificationException.class, () -> skillDemandService.deleteSkillById(id));
        verify(skillDemandRepository, times(0)).delete(any());
    }


    @Test
    public void testDeleteSkill_shouldSucceedForValidInput() {
        // Arrange
        long id = 1L;

        Skill skill = SkillOffer.builder()
                .schedule("Some preexisting schedule")
                .title("Some existing Demand")
                .category(SkillCategory.builder()
                        .id(1000L)
                        .description("Not Blabla")
                        .name("Not Bla")
                        .build())
                .description("This demand already exists")
                .byUser(testUser)
                .id(id)
                .build();

        when(skillRepository.findById(id)).thenReturn(skill);

        // Act
        skillService.deleteSkillById(id);

        // Assert
        verify(skillRepository).delete(skill);
    }


    @Test
    public void testDeleteSkillIncorrectUser_shouldFail() {
        // Arrange
        long id = 1L;

        Skill skill = SkillDemand.builder()
                .urgency(DemandUrgency.LOW)
                .title("Some existing Demand")
                .category(SkillCategory.builder()
                        .id(1000L)
                        .description("Not Blabla")
                        .name("Not Bla")
                        .build())
                .description("This demand already exists")
                .byUser(User.builder()
                        .email("not.thetest.user@user.com")
                        .username("other_user")
                        .build())
                .id(id)
                .build();

        when(skillRepository.findById(id)).thenReturn(skill);

        // Act & Assert
        Assertions.assertThrows(UnauthorizedModificationException.class, () -> skillService.deleteSkillById(id));
        verify(skillRepository, times(0)).delete(any());
    }

    @ParameterizedTest
    @MethodSource("getCreateSkillOfferDTOs")
    public void testUpdateNonExistentSkillOffer_shouldFail(CreateSkillOfferDTO createSkillOfferDTO) {
        // Arrange
        Long id = 1L;
        when(skillOfferRepository.findById(id)).thenReturn(null);

        // Act & Assert
        Assertions.assertThrows(SkillDoesNotExistException.class, () -> skillOfferService.updateSkill(id, createSkillOfferDTO));
    }

    @ParameterizedTest
    @MethodSource("getCreateSkillDemandDTOs")
    public void testUpdateNonExistentSkillDemand_shouldFail(CreateSkillDemandDTO createSkillDemandDTO) {
        // Arrange
        Long id = 1L;
        when(skillDemandRepository.findById(id)).thenReturn(null);

        // Act & Assert
        Assertions.assertThrows(SkillDoesNotExistException.class, () -> skillDemandService.updateSkill(id, createSkillDemandDTO));
    }

    @Test
    public void testDeleteNonExistentSkills_shouldFail() {
        // Arrange
        Long id = 1L;
        when(skillDemandRepository.findById(id)).thenReturn(null);
        when(skillRepository.findById(id)).thenReturn(null);
        when(skillOfferRepository.findById(id)).thenReturn(null);

        // Act & Assert
        Assertions.assertThrows(SkillDoesNotExistException.class, () -> skillDemandService.deleteSkillById(id));
        Assertions.assertThrows(SkillDoesNotExistException.class, () -> skillService.deleteSkillById(id));
        Assertions.assertThrows(SkillDoesNotExistException.class, () -> skillOfferService.deleteSkillById(id));
    }
}
