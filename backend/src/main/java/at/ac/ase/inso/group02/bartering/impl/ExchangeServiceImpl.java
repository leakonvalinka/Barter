package at.ac.ase.inso.group02.bartering.impl;

import at.ac.ase.inso.group02.authentication.AuthenticationService;
import at.ac.ase.inso.group02.authentication.UserRepository;
import at.ac.ase.inso.group02.authentication.UserService;
import at.ac.ase.inso.group02.authentication.dto.UserDetailDTO;
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
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.exchange.ExchangeChat;
import at.ac.ase.inso.group02.entities.exchange.ExchangeItem;
import at.ac.ase.inso.group02.messaging.MessagingService;
import at.ac.ase.inso.group02.rating.RatingService;
import at.ac.ase.inso.group02.rating.dto.CreateRatingDTO;
import at.ac.ase.inso.group02.rating.dto.UserRatingDTO;
import at.ac.ase.inso.group02.skills.SkillService;
import at.ac.ase.inso.group02.skills.dto.SkillDTO;
import at.ac.ase.inso.group02.util.MapperUtil;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import io.quarkus.logging.Log;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@ApplicationScoped
@AllArgsConstructor
public class ExchangeServiceImpl implements ExchangeService {

    private ExchangeRepository exchangeRepository;
    private MessagingService messagingService;
    private ExchangeChatRepository exchangeChatRepository;
    private AuthenticationService authenticationService;
    private UserService userService;
    private UserRepository userRepository;
    private RatingService ratingService;

    private SkillService skillService;

    @Override
    @Transactional
    public ExchangeItemDTO getExchangeItem(Long exchangeItemID) {
        User user = authenticationService.getCurrentUser();
        ExchangeItem exchangeItem = getExchangeItemEntityById(exchangeItemID);

        Skill exchangedSkill = exchangeItem.getExchangedSkill();
        if (!user.equals(exchangeItem.getInitiator()) && !user.equals(exchangedSkill.getByUser())) {
            throw new NotPartOfExchangeException("You are not part of this exchange");
        }

        return mapToExchangeItemDTO(exchangeItem);
    }

    private ExchangeItem getExchangeItemEntityById(Long exchangeItemID) {
        Log.infov("Finding exchange for id {0}", exchangeItemID);
        ExchangeItem exchangeItem = exchangeRepository.findById(exchangeItemID);

        if (exchangeItem == null) {
            throw new NotFoundException("No exchange found with id " + exchangeItemID);
        }

        return exchangeItem;
    }

    private ExchangeChat getExchangeChatEntityById(UUID exchangeChatID) {
        Log.infov("Finding exchange-chat for id {0}", exchangeChatID);
        ExchangeChat exchangeChat = exchangeChatRepository.findById(exchangeChatID);

        if (exchangeChat == null) {
            throw new NotFoundException("No exchange-chat found with id " + exchangeChatID);
        }

        return exchangeChat;
    }

    private ExchangeItemDTO mapToExchangeItemDTO(ExchangeItem exchangeItem) {
        ExchangeItemDTO dto = MapperUtil.map(exchangeItem, ExchangeItemDTO.class);

        // manually set @JsonBackReferences:
        dto.setInitiator(MapperUtil.map(exchangeItem.getInitiator(), UserDetailDTO.class));
        dto.setExchangedSkill(mapSkillWithUser(exchangeItem.getExchangedSkill()));
        if (exchangeItem.getExchangedSkillCounterpart() != null) {
            dto.setExchangedSkillCounterpart(mapSkillWithUser(exchangeItem.getExchangedSkillCounterpart()));
        }
        return dto;
    }

    protected final SkillDTO mapSkillWithUser(Skill skill) {
        SkillDTO dto = MapperUtil.map(skill, new TypeReference<>() {
        });

        // we need to manually map the user, because Jackson's cyclic serialization prevention is just:
        // "I won't serialize one way of the cyclic references automatically"
        dto.setByUser(MapperUtil.map(skill.getByUser(), new TypeReference<>() {
        }));

        return dto;
    }

    @Override
    @Transactional
    public ExchangeItemDTO finalizeExchange(Long exchangeId) {
        User user = authenticationService.getCurrentUser();
        ExchangeItem exchangeItem = getExchangeItemEntityById(exchangeId);

        if (user.equals(exchangeItem.getInitiator())) {
            exchangeItem.setInitiatorMarkedComplete(true);
        } else if (user.equals(exchangeItem.getExchangedSkill().getByUser())) {
            exchangeItem.setResponderMarkedComplete(true);
        } else {
            throw new NotPartOfExchangeException("You cannot finalize this exchange because you are not part of it");
        }

        if (exchangeItem.isInitiatorMarkedComplete() && exchangeItem.isResponderMarkedComplete()) {
            exchangeItem.setRatable(true);
            exchangeItem.setNumberOfExchanges(exchangeItem.getNumberOfExchanges() + 1);
        }

        exchangeRepository.persistAndFlush(exchangeItem);

        return mapToExchangeItemDTO(exchangeItem);
    }

    @Override
    @Transactional
    public UserRatingDTO createRatingForExchange(Long exchangeID, CreateRatingDTO rating) {
        return ratingService.createRatingForExchange(exchangeID, rating);
    }

    private static boolean isExchangeFinalized(ExchangeItem exchangeItem) {
        return exchangeItem.isRatable();
    }

    @Override
    @Transactional
    public void onExchangeExpiry(Long exchangeId) {
        Log.infov("Exchange Activity expired for ID {0}", exchangeId);
        ExchangeItem exchangeItem;
        try {
            exchangeItem = getExchangeItemEntityById(exchangeId);
        } catch (NotFoundException e) {
            return;
        }
        onExchangeExpiry(exchangeItem);
    }

    @Override
    @Transactional
    public void onExchangeChatExpiry(UUID exchangeChatID) {
        ExchangeChat exchangeChat;
        try {
            exchangeChat = getExchangeChatEntityById(exchangeChatID);
        } catch (NotFoundException e) {
            return;
        }

        if (exchangeChat.getRequiredResponders().isEmpty()) {
            Log.infov("Exchange Chat expired for ID {0}", exchangeChatID);
            exchangeChat.getExchangeItems().forEach(this::onExchangeExpiry);
        } else {
            Log.infov("Exchange Chat expired for ID {0} but was never confirmed, not finalizing", exchangeChatID);
        }
    }

    private void onExchangeExpiry(ExchangeItem exchangeItem) {
        exchangeItem.setRatable(true);
        exchangeRepository.persistAndFlush(exchangeItem);
    }

    @Override
    @Transactional
    public ExchangeChatDTO initiateExchange(InitiateExchangesDTO initiateExchangesDTO) {
        AtomicReference<UUID> exchangeChatIDRef = new AtomicReference<>();

        /*
         * wraps the processing of the Exchange-Chat in a new transaction.
         * After this method terminates, the Exchange-Chat is guaranteed persisted
         * (and messages can be written for it)
         */
        QuarkusTransaction.requiringNew().run(() -> {
            User initiator = authenticationService.getCurrentUser();

            ExchangeChat exchangeChat = ExchangeChat.builder()
                    .initiator(initiator)
                    .exchangeItems(new HashSet<>())
                    .build();

            // make sure the ExchangeChat is persisted before sending messages
            ExchangeChat persistedExchangeChat = processAndPersistExchangeChat(initiateExchangesDTO, exchangeChat, initiator, initiator);
            exchangeChatIDRef.set(persistedExchangeChat.getId());
        });

        ExchangeChat persistedExchangeChat = exchangeChatRepository.findById(exchangeChatIDRef.get());

        messagingService.newMessage(persistedExchangeChat.getId().toString(), initiateExchangesDTO.getChatMessage());
        return mapExchangeChatForUser(persistedExchangeChat, authenticationService.getCurrentUser());
    }

    @Override
    @Transactional
    public ExchangeChatDTO updateExchange(UUID exchangeChatID, InitiateExchangesDTO exchangeDTO) {
        AtomicReference<UUID> exchangeChatIDRef = new AtomicReference<>();

        /*
         * wraps the processing of the Exchange-Chat in a new transaction.
         * After this method terminates, the Exchange-Chat is guaranteed persisted
         * (and messages can be written for it)
         */
        QuarkusTransaction.requiringNew().run(() -> {
            ExchangeChat exchangeChat = getExchangeChatEntityById(exchangeChatID);
            User authenticatedUser = authenticationService.getCurrentUser();

            if (exchangeChat.getExchangeItems().stream().noneMatch(
                    exchangeItem -> authenticatedUser.equals(exchangeItem.getInitiator())
                            || authenticatedUser.equals(exchangeItem.getExchangedSkill().getByUser()))
            ) {
                throw new NotPartOfExchangeException("You are not part of this exchange!");
            }

            ExchangeChat persistedExchangeChat = processAndPersistExchangeChat(exchangeDTO, exchangeChat, exchangeChat.getInitiator(), authenticatedUser);
            exchangeChatIDRef.set(persistedExchangeChat.getId());
        });

        ExchangeChat persistedExchangeChat = exchangeChatRepository.findById(exchangeChatIDRef.get());

        messagingService.newMessageForUpdatedExchange(persistedExchangeChat, exchangeDTO.getChatMessage());
        return mapExchangeChatForUser(persistedExchangeChat, authenticationService.getCurrentUser());
    }

    private ExchangeChat processAndPersistExchangeChat(InitiateExchangesDTO initiateExchangesDTO, ExchangeChat exchangeChat, User initiator, User authenticatedUser) {

        processExchangeChat(initiateExchangesDTO, exchangeChat, initiator);
        setRequiredResponders(exchangeChat, authenticatedUser);

        return exchangeChat;
    }

    private void setRequiredResponders(ExchangeChat exchangeChat, User creator) {
        // whenever an Exchange-Chat is created or updated,
        // it needs a response from all other participating users to be considered "active"
        exchangeChat.setRequiredResponders(
                getUsersInvolved(exchangeChat.getExchangeItems())
                        .stream()
                        .filter(user -> !creator.equals(user))
                        .collect(Collectors.toSet())
        );
        exchangeChatRepository.persistAndFlush(exchangeChat);
    }

    private ExchangeChat processExchangeChat(InitiateExchangesDTO initiateExchangesDTO, ExchangeChat exchangeChat, User initiator) {
        Set<ExchangeItem> exchangeItems = getOrCreateExchangeItems(exchangeChat, initiateExchangesDTO, initiator);

        Set<ExchangeItem> itemsMarkedForRemoval = new HashSet<>(exchangeChat.getExchangeItems());
        itemsMarkedForRemoval.removeAll(exchangeItems);

        if (itemsMarkedForRemoval.stream().anyMatch(ExchangeServiceImpl::isExchangeFinalized)) {
            throw new IllegalExchangeModificationException("Cannot remove at least one exchange because it is already finalized");
        }

        Set<User> usersInvolvedBefore = getUsersInvolved(exchangeChat.getExchangeItems());
        Set<User> usersInvolvedAfter = getUsersInvolved(exchangeItems);

        if (exchangeChatRepository.isPersistent(exchangeChat) && !usersInvolvedBefore.equals(usersInvolvedAfter)) {
            throw new IllegalExchangeModificationException("Cannot remove or add users to an existing exchange");
        }

        itemsMarkedForRemoval.forEach(exchangeItem -> exchangeRepository.delete(exchangeItem));

        exchangeChat.getExchangeItems().clear();
        exchangeChat.getExchangeItems().addAll(exchangeItems);

        exchangeChatRepository.persistAndFlush(exchangeChat);

        // set the other side of the relation manually
        exchangeItems.forEach(exchangeItem -> {
            exchangeItem.setExchangeChat(exchangeChat);
            exchangeRepository.persist(exchangeItem);
        });

        exchangeRepository.flush();

        return exchangeChat;
    }

    private static Set<User> getUsersInvolved(Set<ExchangeItem> exchangeItems) {
        return exchangeItems
                .stream()
                .flatMap(exchangeItem -> Stream.of(
                        exchangeItem.getInitiator(),
                        exchangeItem.getExchangedSkill().getByUser()))
                .collect(Collectors.toSet());
    }

    private Set<ExchangeItem> getOrCreateExchangeItems(ExchangeChat exchangeChat, InitiateExchangesDTO initiateExchangesDTO, User initiator) {
        List<ExchangeItem> exchangeItems = initiateExchangesDTO.getExchanges().stream()
                .map(createExchangeDTO -> getOrCreateSingleExchange(exchangeChat, createExchangeDTO, initiator))
                .toList();

        HashSet<ExchangeItem> exchangeItemsSet = new HashSet<>(exchangeItems);

        if (!isUserPartOfExchange(initiator, exchangeItemsSet)) {
            throw new IllegalExchangeException("You must partake in an exchange!");
        }

        if (exchangeItems.stream()
                .map(item -> new AbstractMap.SimpleEntry<>(item.getExchangedSkill(), item.getInitiator()))
                .collect(Collectors.toSet())
                .size() < exchangeItems.size()) {
            throw new IllegalExchangeException("There are duplicate exchanges! The exchange of one skill with one user can only occur once in an exchange!");
        }
        return exchangeItemsSet;
    }

    @Override
    public boolean isUserPartOfExchange(User user, Set<ExchangeItem> exchangeItems) {
        return exchangeItems.stream().anyMatch(exchange -> user.equals(exchange.getExchangedSkill().getByUser())
                || user.equals(exchange.getInitiator()));
    }

    @Override
    public Set<User> getExchangeParticipants(Set<ExchangeItem> exchanges) {
        return exchanges
                .stream()
                .flatMap(exchangeItem -> Stream.of(exchangeItem.getExchangedSkill().getByUser(), exchangeItem.getInitiator()))
                .collect(Collectors.toSet());
    }

    private ExchangeItem getOrCreateSingleExchange(ExchangeChat exchangeChat, CreateExchangeDTO createExchangeDTO, User initiator) {
        Skill exchangedSkill = skillService.getSkillEntityById(createExchangeDTO.getSkillID());

        User singleExchangeInitiator = null;
        if (createExchangeDTO.getForUser() != null) {
            singleExchangeInitiator = userService.getUserEntityByUsername(createExchangeDTO.getForUser());
        }

        Skill skillCounterpart = null;
        if (createExchangeDTO.getSkillCounterPartID() != null) {
            skillCounterpart = skillService.getSkillEntityById(createExchangeDTO.getSkillCounterPartID());

            validateSkillCounterpart(singleExchangeInitiator, skillCounterpart, exchangedSkill);

            singleExchangeInitiator = skillCounterpart.getByUser();
        }

        if (createExchangeDTO.getForUser() == null && createExchangeDTO.getSkillCounterPartID() == null) {
            singleExchangeInitiator = initiator;
        }

        if (singleExchangeInitiator.equals(initiator) && initiator.equals(exchangedSkill.getByUser())) {
            throw new IllegalExchangeException("You cannot exchange a skill with yourself!");
        }

        if (singleExchangeInitiator.equals(exchangedSkill.getByUser())) {
            throw new IllegalExchangeException("Another user cannot exchange a skill with themselves");
        }

        // return existing item if one matches (these fields (exchangeChat, exchangedSkill, initiator) together are UNIQUE)
        Optional<ExchangeItem> existingExchangeItem = exchangeRepository.findExistingExchangeItem(
                exchangeChatRepository.isPersistent(exchangeChat) ? exchangeChat : null,
                exchangedSkill,
                singleExchangeInitiator
        );

        // if there is one such item, it is possible that we want to edit the counterpart-skill
        if (existingExchangeItem.isPresent()) {
            ExchangeItem exchangeItem = existingExchangeItem.get();
            if(!Objects.equals(skillCounterpart, exchangeItem.getExchangedSkillCounterpart())){
                if(isExchangeFinalized(exchangeItem)){
                    throw new IllegalExchangeModificationException("Cannot edit at least one exchange because it is already finalized");
                }
                exchangeItem.setExchangedSkillCounterpart(skillCounterpart);
            }
            return exchangeItem;
        }

        return ExchangeItem.builder()
                .exchangedSkill(exchangedSkill)
                .numberOfExchanges(0)
                .initiator(singleExchangeInitiator)
                .exchangedSkillCounterpart(skillCounterpart)
                .build();
    }

    private static void validateSkillCounterpart(User forUser, Skill skillCounterpart, Skill exchangedSkill) {
        if (forUser != null && !forUser.equals(skillCounterpart.getByUser())) {
            throw new IllegalExchangeException("The initiator for skill " + exchangedSkill.getId() + " does not correspond to the creator of the skill-counterpart");
        }

        if (skillCounterpart.getClass().equals(exchangedSkill.getClass())) {
            throw new IllegalExchangeException("The counterpart to an exchanged skill is the same type (demand/offer) as the exchanged skill itself!");
        }
    }

    @Override
    @Transactional
    public PaginatedQueryDTO<ExchangeChatDTO> getExchangeChatsForCurrentUser(PaginationParamsDTO paginationParamsDTO) {
        User user = authenticationService.getCurrentUser();
        Log.infov("Fetching exchange chats for current user {0} with pagination params: {1}", user.getUsername(), paginationParamsDTO);

        return PaginationUtil.getPaginatedQueryDTO(
                paginationParamsDTO,
                exchangeChatRepository.findByUser(user),
                exchangeChat -> this.mapExchangeChatForUser(exchangeChat, user)
        );
    }

    @Override
    @Transactional
    public PaginatedQueryDTO<ExchangeChatDTO> getExchangeChatsByUsername(String username, PaginationParamsDTO paginationParamsDTO) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new NotFoundException("User not found: " + username);
        }
    
        return PaginationUtil.getPaginatedQueryDTO(
                paginationParamsDTO,
                exchangeChatRepository.findByUser(user),
                exchangeChat -> this.mapExchangeChatForUser(exchangeChat, user)
        );
    }

    @Override
    public ExchangeChatDTO getExchangeChat(String exchangeChatID) {
        User user = authenticationService.getCurrentUser();
        ExchangeChat exchangeChat;
        try {
            exchangeChat = getExchangeChatEntityById(UUID.fromString(exchangeChatID));
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException("Could not find Exchange-Chat with that ID!");
        }

        if (!isUserPartOfExchange(user, exchangeChat.getExchangeItems())) {
            throw new NotPartOfExchangeException("You cannot fetch data of an Exchange you are not part of!");
        }

        return mapExchangeChatForUser(exchangeChat, user);
    }

    private ExchangeChatDTO mapExchangeChatForUser(ExchangeChat exchangeChat, User user) {
        return ExchangeChatDTO.builder()
                .id(exchangeChat.getId())
                .exchanges(exchangeChat.getExchangeItems().stream().map(this::mapToExchangeItemDTO).collect(Collectors.toSet()))
                .initiator(MapperUtil.map(exchangeChat.getInitiator(), UserDetailDTO.class))
                .confirmationResponsePending(exchangeChat.getRequiredResponders().contains(user))
                .mostRecentMessage(messagingService.getMostRecentMessageForExchange(exchangeChat))
                .numberOfUnseenMessages(messagingService.getUnreadMessagesCount(exchangeChat, user))
                .build();
    }
}
