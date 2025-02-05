package at.ac.ase.inso.group02.bartering;

import at.ac.ase.inso.group02.bartering.dto.ExchangeChatDTO;
import at.ac.ase.inso.group02.bartering.dto.ExchangeItemDTO;
import at.ac.ase.inso.group02.bartering.dto.InitiateExchangesDTO;
import at.ac.ase.inso.group02.bartering.exception.IllegalExchangeException;
import at.ac.ase.inso.group02.bartering.exception.IllegalExchangeModificationException;
import at.ac.ase.inso.group02.bartering.exception.NotPartOfExchangeException;
import at.ac.ase.inso.group02.entities.User;
import at.ac.ase.inso.group02.entities.exchange.ExchangeChat;
import at.ac.ase.inso.group02.entities.exchange.ExchangeItem;
import at.ac.ase.inso.group02.rating.dto.CreateRatingDTO;
import at.ac.ase.inso.group02.rating.dto.UserRatingDTO;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;
import jakarta.ws.rs.NotFoundException;

import java.util.Set;
import java.util.UUID;

/**
 * service for operations on skill-exchanges.
 * Both for entire Exchanges (ExchangeChat) or single items of exchanges (ExchangeItem)
 * corresponding to a single service that is exchanged during an exchange
 */
public interface ExchangeService {

    /**
     * retrieves a single exchange-item by its ID that the authenticated user is part of
     * @param exchangeId ID of the exchange-item
     * @return an exchange-item with the given ID
     * @throws NotPartOfExchangeException if the authenticated user is not part of the single exchange (neither receives nor provides the exchanged skill)
     * @throws NotFoundException if there is no exchange-item with the given ID
     */
    ExchangeItemDTO getExchangeItem(Long exchangeId);

    /**
     * marks a single exchange-item (identified by ID) as complete by the authenticated user
     * @param exchangeId ID of the exchange-item
     * @return updated state of the exchange-item
     * @throws NotPartOfExchangeException if the authenticated user is not part of the single exchange (neither receives nor provides the exchanged skill)
     * @throws NotFoundException if there is no exchange-item with the given ID
     */
    ExchangeItemDTO finalizeExchange(Long exchangeId);

    /**
     * creates a rating for an exchange-item (identified by ID)  by the authenticated user
     * @param exchangeID ID of the exchange-item
     * @param rating rating-data provided by the authenticated user
     * @return data of the created rating
     * refer to {@link at.ac.ase.inso.group02.rating.RatingService#createRatingForExchange(Long, CreateRatingDTO) RatingService.createRatingForExchange()} for exceptions
     */
    UserRatingDTO createRatingForExchange(Long exchangeID, CreateRatingDTO rating);

    /**
     * performs the necessary actions to update an exchange-item when its exchange-chat expired (after 3 days without any new messages)
     * @param exchangeId ID of the exchange-item
     */
    void onExchangeExpiry(Long exchangeId);

    /**
     * performs the necessary actions to update an exchange-chat when it expires (after 3 days without any new messages)
     * @param exchangeChatID ID of the exchange-chat
     */
    void onExchangeChatExpiry(UUID exchangeChatID);

    /**
     * creates an exchange initiated by the authenticated user with the provided data
     * @param createExchangeDTO exchange data (which skills are exchanged)
     * @return data of the new Exchange-Chat for this exchange
     * @throws IllegalExchangeException if any of the following is true:
     * - the authenticated user does not partake in any of the skill-exchanges
     * - there are (implicit) duplicate skill-exchanges provided (same skill for same user)
     * - a skill is exchanged with the creator of that skill
     * - skillCounterPartID does not correspond to a skill by user "forUser" for any of the exchanges
     * - skillCounterPartID corresponds to a skill of the same type (offer/demand) as the exchangedSkill (skillID) itself
     */
    ExchangeChatDTO initiateExchange(InitiateExchangesDTO createExchangeDTO);

    /**
     * updates an exchange with new skill-exchanges
     * @param exchangeChatID ID of the exchange-chat to update
     * @param exchangeDTO new exchange data (which skills are exchanged)
     * @return data of the new Exchange-Chat for this updated exchange
     * @throws NotFoundException if no Exchange-Chat with the provided ID exists
     * @throws IllegalExchangeException as {@link ExchangeService#initiateExchange(InitiateExchangesDTO)}
     * @throws IllegalExchangeModificationException if this modification would remove an exchange-item that
     * is already finalized (manually by both parties or automatically after 3 days of inactivity)
     * or the modification would remove/add a user from/to the exchange
     * @throws NotPartOfExchangeException if the authenticated user is not part of the exchange
     * -
     */
    ExchangeChatDTO updateExchange(UUID exchangeChatID, InitiateExchangesDTO exchangeDTO);

    /**
     * retrieves the exchange-chats for the currently authenticated user
     * @param paginationParamsDTO parameters for pagination
     * @return exchange-chats for the currently authenticated user, paginated
     */
    PaginatedQueryDTO<ExchangeChatDTO> getExchangeChatsForCurrentUser(PaginationParamsDTO paginationParamsDTO);

    /**
     * retrieves the exchange-chats for the specified user
     * @param username username of the specified user
     * @param paginationParamsDTO parameters for pagination
     * @return exchange-chats for the specified user, paginated
     */
    PaginatedQueryDTO<ExchangeChatDTO> getExchangeChatsByUsername(String username, PaginationParamsDTO paginationParamsDTO);

    /**
     * retrieves a single Exchange-Chat by its id
     * @param exchangeChatID id of the Exchange-Chat
     * @return the Exchange-Chat as a DTO
     * @throws NotPartOfExchangeException if the currently authenticated user is not part of the exchange
     * @throws NotFoundException if there is no such Exchange-Chat
     */
    ExchangeChatDTO getExchangeChat(String exchangeChatID);

    /**
     * returns true iff the user is part of (any of) the given ExchangeItems
     * @param user user to check participation for
     * @param exchangeItems exchange-Items representing an exchange
     * @return true iff the user is part of (any of) the given exchangeItems
     */
    boolean isUserPartOfExchange(User user, Set<ExchangeItem> exchangeItems);

    /**
     * extracts all participants of a given set of ExchangeItems
     * (a participant either receives or offers a skill in an exchange)
     * @param exchanges exchanges to extract participants from
     * @return all participants in the exchanges
     */
    Set<User> getExchangeParticipants(Set<ExchangeItem> exchanges);
}
