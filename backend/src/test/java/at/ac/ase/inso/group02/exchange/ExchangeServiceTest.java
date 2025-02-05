package at.ac.ase.inso.group02.exchange;

import at.ac.ase.inso.group02.authentication.AuthenticationService;
import at.ac.ase.inso.group02.authentication.UserRepository;
import at.ac.ase.inso.group02.authentication.UserService;
import at.ac.ase.inso.group02.bartering.ExchangeChatRepository;
import at.ac.ase.inso.group02.bartering.ExchangeRepository;
import at.ac.ase.inso.group02.bartering.ExchangeService;
import at.ac.ase.inso.group02.bartering.dto.CreateExchangeDTO;
import at.ac.ase.inso.group02.bartering.dto.ExchangeChatDTO;
import at.ac.ase.inso.group02.bartering.dto.ExchangeItemDTO;
import at.ac.ase.inso.group02.bartering.dto.InitiateExchangesDTO;
import at.ac.ase.inso.group02.bartering.exception.IllegalExchangeException;
import at.ac.ase.inso.group02.bartering.exception.IllegalExchangeModificationException;
import at.ac.ase.inso.group02.bartering.exception.NotPartOfExchangeException;
import at.ac.ase.inso.group02.entities.Skill;
import at.ac.ase.inso.group02.entities.SkillDemand;
import at.ac.ase.inso.group02.entities.SkillOffer;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.exchange.ExchangeChat;
import at.ac.ase.inso.group02.entities.exchange.ExchangeItem;
import at.ac.ase.inso.group02.messaging.MessagingService;
import at.ac.ase.inso.group02.messaging.dto.NewChatMessageDTO;
import at.ac.ase.inso.group02.skills.SkillService;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Slf4j
@QuarkusTest
public class ExchangeServiceTest {
    @Inject
    @InjectMocks
    ExchangeService exchangeService;

    @InjectMock
    UserService userService;

    @InjectMock
    AuthenticationService authenticationServiceMock;

    @InjectMock
    ExchangeRepository exchangeRepositoryMock;

    @InjectMock
    ExchangeChatRepository exchangeChatRepositoryMock;

    @InjectMock
    SkillService skillService;

    @InjectMock
    MessagingService messagingServiceMock;

    @InjectMock
    UserRepository userRepository;

    static User autheticatedUser = User.builder()
            .username("authenticatedUser")
            .build();
    static User user2 = User.builder()
            .username("user2")
            .build();
    static User user3 = User.builder()
            .username("user3")
            .build();

    static Skill skillAuthenticatedUserOffer = SkillOffer.builder()
            .id(100L)
            .title("Skill Offer by authenticated User")
            .byUser(autheticatedUser)
            .build();

    static Skill skillAuthenticatedUserDemand = SkillDemand.builder()
            .id(101L)
            .title("Skill Demand by authenticated User")
            .byUser(autheticatedUser)
            .build();

    static Skill skillUser2Offer = SkillOffer.builder()
            .id(102L)
            .title("Skill Offer by User 2")
            .byUser(user2)
            .build();
    static Skill skillUser2Demand = SkillDemand.builder()
            .id(103L)
            .title("Skill Demand by User 2")
            .byUser(user2)
            .build();
    static Skill skillUser3Demand = SkillDemand.builder()
            .id(104L)
            .title("Skill Demand by User 3")
            .byUser(user3)
            .build();

    @BeforeEach
    void setup() {
        when(authenticationServiceMock.getCurrentUser()).thenReturn(autheticatedUser);

        Set.of(autheticatedUser, user2, user3)
                .forEach(user ->
                        when(userService.getUserEntityByUsername(user.getUsername()))
                                .thenReturn(user));

        Set.of(skillAuthenticatedUserOffer, skillAuthenticatedUserDemand, skillUser2Offer, skillUser2Demand, skillUser3Demand)
                .forEach(skill ->
                        when(skillService.getSkillEntityById(skill.getId()))
                                .thenReturn(skill));

        // when persisting, randomly set the ID of the ExchangeChat to prevent NullPointerExceptions
        doAnswer((Answer<Void>) invocation -> {
            ExchangeChat exchangeChat = invocation.getArgument(0);
            UUID id = UUID.randomUUID();
            exchangeChat.setId(id);

            // and answer any queries for this id that follow with the correct entity
            when(exchangeChatRepositoryMock.findById(id)).thenReturn(exchangeChat);
            return null;
        }).when(exchangeChatRepositoryMock).persistAndFlush(any());

    }

    @Test
    void testGetExchangeItem_shouldReturnCorrectItem() {
        // Arrange
        Long id = -1L;
        Skill exchangedSkill = SkillOffer.builder().byUser(autheticatedUser).build();
        ExchangeItem exchangeItem = ExchangeItem.builder()
                .exchangedSkill(exchangedSkill)
                .initiator(user2)
                .id(id)
                .build();

        when(exchangeRepositoryMock.findById(id)).thenReturn(exchangeItem);

        // Act
        ExchangeItemDTO dto = exchangeService.getExchangeItem(id);

        // Assert
        assertEquals(exchangedSkill.getId(), dto.getExchangedSkill().getId());
        assertEquals(id, dto.getId());
        assertEquals(user2.getUsername(), dto.getInitiator().getUsername());
    }

    @Test
    void testGetExchangeItem_shouldFailNotPartOfExchange() {
        // Arrange
        Long id = -1L;
        // authenticated-user is not part of this exchange, since they neither offer the skill nor initiated the exchange
        Skill exchangedSkill = SkillOffer.builder().byUser(user2).build();
        ExchangeItem exchangeItem = ExchangeItem.builder()
                .exchangedSkill(exchangedSkill)
                .initiator(user3)
                .id(id)
                .build();

        when(exchangeRepositoryMock.findById(id)).thenReturn(exchangeItem);

        // Act & Assert
        assertThrows(NotPartOfExchangeException.class, () -> exchangeService.getExchangeItem(id));
    }

    @Test
    void testGetExchangeItem_shouldFailNoSuchExchange() {
        // Arrange
        Long id = -1L;
        when(exchangeRepositoryMock.findById(id)).thenReturn(null);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> exchangeService.getExchangeItem(id));
    }

    static Stream<InitiateExchangesDTO> validExchanges() {
        // Arrange
        return Stream.of(
                InitiateExchangesDTO.builder()
                        .exchanges(Set.of(
                                CreateExchangeDTO.builder()
                                        .skillID(skillUser2Offer.getId())
                                        .build()
                        ))
                        .chatMessage(NewChatMessageDTO.builder().content("Test-Exchange Message").build())
                        .build(),
                InitiateExchangesDTO.builder()
                        .exchanges(Set.of(
                                CreateExchangeDTO.builder()
                                        .skillID(skillAuthenticatedUserOffer.getId())
                                        .skillCounterPartID(skillUser2Demand.getId())
                                        .build(),
                                CreateExchangeDTO.builder()
                                        .skillID(skillAuthenticatedUserDemand.getId())
                                        .skillCounterPartID(skillUser2Offer.getId())
                                        .build()
                        ))
                        .chatMessage(NewChatMessageDTO.builder().content("Test-Exchange Message").build())
                        .build(),
                InitiateExchangesDTO.builder()
                        .exchanges(Set.of(
                                CreateExchangeDTO.builder()
                                        .skillID(skillAuthenticatedUserOffer.getId())
                                        .skillCounterPartID(skillUser2Demand.getId())
                                        .build(),
                                CreateExchangeDTO.builder()
                                        .skillID(skillUser2Offer.getId())
                                        .skillCounterPartID(skillAuthenticatedUserDemand.getId())
                                        .build()
                        ))
                        .chatMessage(NewChatMessageDTO.builder().content("Test-Exchange Message").build())
                        .build(),
                InitiateExchangesDTO.builder()
                        .exchanges(Set.of(
                                CreateExchangeDTO.builder()
                                        .skillID(skillAuthenticatedUserOffer.getId())
                                        .forUser(user2.getUsername())
                                        .build(),
                                CreateExchangeDTO.builder()
                                        .skillID(skillUser2Offer.getId())
                                        .build()
                        ))
                        .chatMessage(NewChatMessageDTO.builder().content("Test-Exchange Message").build())
                        .build(),
                InitiateExchangesDTO.builder()
                        .exchanges(Set.of(
                                // triangle also possible
                                // I get from user 2
                                // User 2 gets from user 3
                                // User 3 gets from me
                                CreateExchangeDTO.builder()
                                        .skillID(skillAuthenticatedUserDemand.getId())
                                        .forUser(user2.getUsername())
                                        .build(),
                                CreateExchangeDTO.builder()
                                        .skillID(skillUser2Demand.getId())
                                        .forUser(user3.getUsername())
                                        .build(),
                                CreateExchangeDTO.builder()
                                        .skillID(skillUser3Demand.getId())
                                        .skillCounterPartID(skillAuthenticatedUserOffer.getId())
                                        .build()
                        ))
                        .chatMessage(NewChatMessageDTO.builder().content("Test-Exchange Message").build())
                        .build(),
                InitiateExchangesDTO.builder()
                        .exchanges(Set.of(
                                CreateExchangeDTO.builder()
                                        .skillID(skillUser2Demand.getId())
                                        .build(),
                                CreateExchangeDTO.builder()
                                        .skillID(skillUser2Offer.getId())
                                        .build()
                        ))
                        .chatMessage(NewChatMessageDTO.builder().content("Test-Exchange Message").build())
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource("validExchanges")
    void testInitiateExchange_shouldSucceed(InitiateExchangesDTO initiateExchangesDTO) {
        // Act
        ExchangeChatDTO dto = exchangeService.initiateExchange(initiateExchangesDTO);

        // Assert
        verifyExchangeDTOResult(initiateExchangesDTO, dto);
        // new message is published
        verify(messagingServiceMock, times(1)).newMessage(eq(dto.getId().toString()),
                argThat(arg -> arg.getContent().equals(initiateExchangesDTO.getChatMessage().getContent()))
        );
    }

    private void verifyExchangeDTOResult(InitiateExchangesDTO initiateExchangesDTO, ExchangeChatDTO dto) {
        initiateExchangesDTO.getExchanges().forEach(exchange -> {
            assertTrue(dto.getExchanges()
                    .stream()
                    .anyMatch(exchangeItem -> exchangeItem
                            .getExchangedSkill()
                            .getId()
                            .equals(exchange.getSkillID())
                            &&
                            (exchange.getSkillCounterPartID() == null ||
                                    exchangeItem
                                            .getInitiator()
                                            .getUsername()
                                            .equals(skillService.getSkillEntityById(exchange.getSkillCounterPartID())
                                                    .getByUser()
                                                    .getUsername()))
                            &&
                            (exchange.getForUser() == null ||
                                    exchangeItem
                                            .getInitiator()
                                            .getUsername()
                                            .equals(exchange.getForUser()))

                    ));
        });
    }


    static Stream<InitiateExchangesDTO> invalidExchanges() {
        // Arrange
        return Stream.of(
                InitiateExchangesDTO.builder()
                        .exchanges(Set.of(
                                CreateExchangeDTO.builder()
                                        // no participation of the authenticated user
                                        .skillID(skillUser2Offer.getId())
                                        .skillCounterPartID(skillUser3Demand.getId())
                                        .build()
                        ))
                        .chatMessage(NewChatMessageDTO.builder().content("Test-Exchange Message").build())
                        .build(),
                InitiateExchangesDTO.builder()
                        .exchanges(Set.of(
                                CreateExchangeDTO.builder()
                                        // the counterpart of an offer must be a demand
                                        .skillID(skillAuthenticatedUserOffer.getId())
                                        .skillCounterPartID(skillUser2Offer.getId())
                                        .build(),
                                CreateExchangeDTO.builder()
                                        .skillID(skillAuthenticatedUserDemand.getId())
                                        .skillCounterPartID(skillUser2Offer.getId())
                                        .build()
                        ))
                        .chatMessage(NewChatMessageDTO.builder().content("Test-Exchange Message").build())
                        .build(),
                InitiateExchangesDTO.builder()
                        .exchanges(Set.of(
                                CreateExchangeDTO.builder()
                                        // cannot exchange skill with myself
                                        .skillID(skillAuthenticatedUserOffer.getId())
                                        .skillCounterPartID(skillAuthenticatedUserDemand.getId())
                                        .build(),
                                CreateExchangeDTO.builder()
                                        .skillID(skillUser2Offer.getId())
                                        .skillCounterPartID(skillAuthenticatedUserDemand.getId())
                                        .build()
                        ))
                        .chatMessage(NewChatMessageDTO.builder().content("Test-Exchange Message").build())
                        .build(),
                InitiateExchangesDTO.builder()
                        .exchanges(Set.of(
                                CreateExchangeDTO.builder()
                                        // cannot exchange skill with themselves
                                        .skillID(skillUser2Demand.getId())
                                        .skillCounterPartID(skillUser2Offer.getId())
                                        .build()
                        ))
                        .chatMessage(NewChatMessageDTO.builder().content("Test-Exchange Message").build())
                        .build(),

                InitiateExchangesDTO.builder()
                        .exchanges(Set.of(
                                CreateExchangeDTO.builder()
                                        // cannot exchange skill with myself
                                        .skillID(skillAuthenticatedUserOffer.getId())
                                        .forUser(autheticatedUser.getUsername())
                                        .build()
                        ))
                        .chatMessage(NewChatMessageDTO.builder().content("Test-Exchange Message").build())
                        .build(),

                InitiateExchangesDTO.builder()
                        .exchanges(Set.of(
                                // These two exchanges model the same thing, the skill exchanged with user2
                                // Duplicates are not allowed!
                                CreateExchangeDTO.builder()
                                        .skillID(skillAuthenticatedUserOffer.getId())
                                        .forUser(user2.getUsername())
                                        .build(),
                                CreateExchangeDTO.builder()
                                        .skillID(skillAuthenticatedUserOffer.getId())
                                        .skillCounterPartID(skillUser2Demand.getId())
                                        .build()
                        ))
                        .chatMessage(NewChatMessageDTO.builder().content("Test-Exchange Message").build())
                        .build()
        );
    }


    @ParameterizedTest
    @MethodSource("invalidExchanges")
    void testInitiateInValidExchange_shouldFailWithIllegalExchangeException(InitiateExchangesDTO initiateExchangesDTO) {
        // Act & Assert
        assertThrows(IllegalExchangeException.class, () -> exchangeService.initiateExchange(initiateExchangesDTO));
        // no message published
        verify(messagingServiceMock, times(0)).newMessage(any(), any());
    }

    static Stream<InitiateExchangesDTO> validExchangeUpdates() {
        // Arrange
        return Stream.of(
                InitiateExchangesDTO.builder()
                        .exchanges(Set.of(
                                CreateExchangeDTO.builder()
                                        .skillID(skillUser2Demand.getId())
                                        .skillCounterPartID(skillAuthenticatedUserOffer.getId())
                                        .build(),

                                CreateExchangeDTO.builder()
                                        .skillID(skillAuthenticatedUserDemand.getId())
                                        //changes this from user2 to user3
                                        .forUser(user3.getUsername())
                                        .build(),

                                CreateExchangeDTO.builder()
                                        .skillID(skillAuthenticatedUserOffer.getId())
                                        .forUser(user3.getUsername())
                                        .build()
                        ))
                        .chatMessage(NewChatMessageDTO.builder().content("Test-Exchange Message").build())
                        .build(),
                InitiateExchangesDTO.builder()
                        .exchanges(Set.of(
                                CreateExchangeDTO.builder()
                                        .skillID(skillUser2Demand.getId())
                                        .skillCounterPartID(skillAuthenticatedUserOffer.getId())
                                        .build(),

                                //changes this skill
                                CreateExchangeDTO.builder()
                                        .skillID(skillUser3Demand.getId())
                                        .forUser(user2.getUsername())
                                        .build(),

                                CreateExchangeDTO.builder()
                                        .skillID(skillAuthenticatedUserOffer.getId())
                                        .forUser(user3.getUsername())
                                        .build()
                        ))
                        .chatMessage(NewChatMessageDTO.builder().content("Test-Exchange Message").build())
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource("validExchangeUpdates")
    void testUpdateExchange_shouldSucceed(InitiateExchangesDTO exchangeUpdateDTO) {
        // Arrange
        ExchangeChat existingExchangeChat = getMockExchangeChat();

        // Act
        ExchangeChatDTO dto = exchangeService.updateExchange(existingExchangeChat.getId(), exchangeUpdateDTO);

        // Assert
        verifyExchangeDTOResult(exchangeUpdateDTO, dto);
        // update message is published
        verify(messagingServiceMock, times(1)).newMessageForUpdatedExchange(eq(existingExchangeChat),
                argThat(arg -> arg.getContent().equals(exchangeUpdateDTO.getChatMessage().getContent()))
        );
    }

    private ExchangeChat getMockExchangeChat() {
        // Arrange
        ExchangeChat existingExchangeChat = ExchangeChat.builder()
                .id(UUID.randomUUID())
                .exchangeItems(new HashSet<>())
                .initiator(user2)
                .build();

        existingExchangeChat.getExchangeItems().addAll(
                Set.of(
                        ExchangeItem.builder()
                                .exchangeChat(existingExchangeChat)
                                .id(-100L)
                                .exchangedSkill(skillUser2Demand)
                                .exchangedSkillCounterpart(skillAuthenticatedUserOffer)
                                .initiator(autheticatedUser)
                                .numberOfExchanges(2)
                                .ratable(true)
                                .initiatorMarkedComplete(true)
                                .responderMarkedComplete(true)
                                .build(),
                        ExchangeItem.builder()
                                .exchangeChat(existingExchangeChat)
                                .id(-101L)
                                .exchangedSkill(skillAuthenticatedUserDemand)
                                .initiator(user2)
                                .numberOfExchanges(0)
                                .build(),
                        ExchangeItem.builder()
                                .exchangeChat(existingExchangeChat)
                                .id(-102L)
                                .exchangedSkill(skillAuthenticatedUserOffer)
                                .initiator(user3)
                                .numberOfExchanges(0)
                                .build()
                )
        );

        when(exchangeChatRepositoryMock.findById(existingExchangeChat.getId())).thenReturn(existingExchangeChat);
        when(exchangeChatRepositoryMock.isPersistent(existingExchangeChat)).thenReturn(true);

        existingExchangeChat.getExchangeItems().forEach(exchangeItem -> {
            when(exchangeRepositoryMock.findExistingExchangeItem(existingExchangeChat, exchangeItem.getExchangedSkill(), exchangeItem.getInitiator()))
                    .thenReturn(Optional.of(exchangeItem));
        });
        return existingExchangeChat;
    }

    @Test
    void testUpdateNonExistingExchange_shouldFailWithNotFoundException() {
        // Arrange
        when(exchangeChatRepositoryMock.findById(any())).thenReturn(null);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> exchangeService.updateExchange(UUID.randomUUID(), InitiateExchangesDTO.builder().build()));
        // no message published
        verify(messagingServiceMock, times(0)).newMessageForUpdatedExchange(any(), any());
    }


    @ParameterizedTest
    @MethodSource("invalidExchanges")
    void testUpdateExchangeWithInvalidExchanges_shouldFailWithIllegalExchangeException(InitiateExchangesDTO exchangeUpdateDTO) {
        // Arrange
        ExchangeChat existingExchangeChat = getMockExchangeChat();

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> exchangeService.updateExchange(existingExchangeChat.getId(), exchangeUpdateDTO));

        assertTrue(exception instanceof IllegalExchangeException ||
                exception instanceof IllegalExchangeModificationException);
        // no message published
        verify(messagingServiceMock, times(0)).newMessageForUpdatedExchange(any(), any());
    }

    static Stream<InitiateExchangesDTO> invalidExchangeUpdates() {
        // Arrange
        return Stream.of(
                InitiateExchangesDTO.builder()
                        .exchanges(Set.of(
                                CreateExchangeDTO.builder()
                                        .skillID(skillUser2Demand.getId())
                                        .skillCounterPartID(skillAuthenticatedUserOffer.getId())
                                        .build(),

                                CreateExchangeDTO.builder()
                                        .skillID(skillAuthenticatedUserDemand.getId())
                                        .forUser(user2.getUsername())
                                        .build()
                                // removes user3 from the exchange
                        ))
                        .chatMessage(NewChatMessageDTO.builder().content("Test-Exchange Message").build())
                        .build(),
                InitiateExchangesDTO.builder()
                        .exchanges(Set.of(
                                // the first exchange cannot be removed since it is already finalized

                                CreateExchangeDTO.builder()
                                        .skillID(skillAuthenticatedUserDemand.getId())
                                        .forUser(user2.getUsername())
                                        .build(),

                                CreateExchangeDTO.builder()
                                        .skillID(skillAuthenticatedUserOffer.getId())
                                        .forUser(user3.getUsername())
                                        .build()
                        ))
                        .chatMessage(NewChatMessageDTO.builder().content("Test-Exchange Message").build())
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource("invalidExchangeUpdates")
    void testUpdateExchangeWithInvalidExchangeUpdates_shouldFailWithIllegalExchangeModificationException(InitiateExchangesDTO exchangeUpdateDTO) {
        // Arrange
        ExchangeChat existingExchangeChat = getMockExchangeChat();

        // Act & Assert
        assertThrows(IllegalExchangeModificationException.class, () -> exchangeService.updateExchange(existingExchangeChat.getId(), exchangeUpdateDTO));
        // no message published
        verify(messagingServiceMock, times(0)).newMessageForUpdatedExchange(any(), any());
    }

    @Test
    void testGetExchangeChatsByUsername_ShouldReturnEmptyPage_WhenNoChatsExist() {
        // Arrange
        String username = user2.getUsername();
        PaginationParamsDTO params = new PaginationParamsDTO();
        params.setPage(0);
        params.setPageSize(10);

        PanacheQuery<ExchangeChat> mockQuery = mock(PanacheQuery.class);
        when(userRepository.findByUsername(username)).thenReturn(user2);
        when(exchangeChatRepositoryMock.findByUser(user2)).thenReturn(mockQuery);
        when(mockQuery.page(any(Page.class))).thenReturn(mockQuery);
        when(mockQuery.stream()).thenReturn(Stream.empty());
        when(mockQuery.count()).thenReturn(0L);
        when(mockQuery.pageCount()).thenReturn(0);
        // Mock the page() method to return the current page
        when(mockQuery.page()).thenReturn(Page.of(0, 10));

        // Act
        PaginatedQueryDTO<ExchangeChatDTO> result = exchangeService.getExchangeChatsByUsername(username, params);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getItems().size());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getPageSize());
        assertFalse(result.isHasMore());
        
        // Verify interactions
        verify(exchangeChatRepositoryMock).findByUser(user2);
        verify(mockQuery).page(any(Page.class));
        verify(mockQuery).page();
        verify(mockQuery).stream();
        verify(mockQuery).count();
        verify(mockQuery).pageCount();
    }

    @Test
    void testGetExchangeChatsByUsername_ShouldReturnCorrectPage_WithMultipleChats() {
        // Arrange
        String username = user2.getUsername();
        PaginationParamsDTO params = new PaginationParamsDTO();
        params.setPage(0);
        params.setPageSize(2);

        ExchangeChat chat1 = ExchangeChat.builder()
                .id(UUID.randomUUID())
                .initiator(user2)
                .exchangeItems(new HashSet<>(Set.of(
                    ExchangeItem.builder()
                        .exchangedSkill(skillUser2Offer)
                        .initiator(autheticatedUser)
                        .build()
                )))
                .build();

        ExchangeChat chat2 = ExchangeChat.builder()
                .id(UUID.randomUUID())
                .initiator(autheticatedUser)
                .exchangeItems(new HashSet<>(Set.of(
                    ExchangeItem.builder()
                        .exchangedSkill(skillAuthenticatedUserOffer)
                        .initiator(user2)
                        .build()
                )))
                .build();

        PanacheQuery<ExchangeChat> mockQuery = mock(PanacheQuery.class);
        when(userRepository.findByUsername(username)).thenReturn(user2);
        when(exchangeChatRepositoryMock.findByUser(user2)).thenReturn(mockQuery);
        when(mockQuery.page(any(Page.class))).thenReturn(mockQuery);
        when(mockQuery.stream()).thenReturn(Stream.of(chat1, chat2));
        when(mockQuery.count()).thenReturn(2L);
        when(mockQuery.pageCount()).thenReturn(1);
        when(mockQuery.page()).thenReturn(Page.of(0, 2));
        when(messagingServiceMock.getMostRecentMessageForExchange(any())).thenReturn(null);
        when(messagingServiceMock.getUnreadMessagesCount(any(), any())).thenReturn(0L);

        // Act
        PaginatedQueryDTO<ExchangeChatDTO> result = exchangeService.getExchangeChatsByUsername(username, params);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getItems().size());
        assertEquals(0, result.getPage());
        assertEquals(2, result.getPageSize());
        assertEquals(2, result.getTotal());
        assertFalse(result.isHasMore());

        // Verify chat content
        List<ExchangeChatDTO> chats = new ArrayList<>(result.getItems());
        assertEquals(chat1.getId(), chats.get(0).getId());
        assertEquals(chat2.getId(), chats.get(1).getId());
        
        // Verify interactions
        verify(exchangeChatRepositoryMock).findByUser(user2);
        verify(mockQuery).page(any(Page.class));
        verify(mockQuery).page();
        verify(mockQuery).stream();
        verify(mockQuery).count();
        verify(mockQuery).pageCount();
    }

    @Test
    void testGetExchangeChatsByUsername_ShouldHandlePagination_WithMultiplePages() {
        // Arrange
        String username = user2.getUsername();
        PaginationParamsDTO params = new PaginationParamsDTO();
        params.setPage(0);
        params.setPageSize(1);

        ExchangeChat chat1 = ExchangeChat.builder()
                .id(UUID.randomUUID())
                .initiator(user2)
                .exchangeItems(new HashSet<>())
                .build();

        PanacheQuery<ExchangeChat> mockQuery = mock(PanacheQuery.class);
        when(userRepository.findByUsername(username)).thenReturn(user2);
        when(exchangeChatRepositoryMock.findByUser(user2)).thenReturn(mockQuery);
        when(mockQuery.page(any(Page.class))).thenReturn(mockQuery);
        when(mockQuery.stream()).thenReturn(Stream.of(chat1));
        when(mockQuery.count()).thenReturn(2L);
        when(mockQuery.pageCount()).thenReturn(2);
        when(mockQuery.page()).thenReturn(Page.of(0, 1));
        when(messagingServiceMock.getMostRecentMessageForExchange(any())).thenReturn(null);
        when(messagingServiceMock.getUnreadMessagesCount(any(), any())).thenReturn(0L);

        // Act
        PaginatedQueryDTO<ExchangeChatDTO> result = exchangeService.getExchangeChatsByUsername(username, params);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(0, result.getPage());
        assertEquals(1, result.getPageSize());
        assertEquals(2, result.getTotal());
        assertTrue(result.isHasMore());
        
        // Verify interactions
        verify(exchangeChatRepositoryMock).findByUser(user2);
        verify(mockQuery).page(any(Page.class));
        verify(mockQuery).page();
        verify(mockQuery).stream();
        verify(mockQuery).count();
        verify(mockQuery).pageCount();
    }

    @Test
    void testGetExchangeChatsByUsername_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        // Arrange
        String username = "nonexistent";
        PaginationParamsDTO params = new PaginationParamsDTO();
        
        when(userRepository.findByUsername(username)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> 
            exchangeService.getExchangeChatsByUsername(username, params)
        );
        
        assertEquals("User not found: " + username, exception.getMessage());
        
        // Verify interactions
        verify(userRepository).findByUsername(username);
        verify(exchangeChatRepositoryMock, never()).findByUser(any());
    }
}
