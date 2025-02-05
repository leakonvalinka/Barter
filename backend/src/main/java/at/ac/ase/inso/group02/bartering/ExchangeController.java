package at.ac.ase.inso.group02.bartering;

import at.ac.ase.inso.group02.bartering.dto.ExchangeChatDTO;
import at.ac.ase.inso.group02.bartering.dto.ExchangeItemDTO;
import at.ac.ase.inso.group02.bartering.dto.InitiateExchangesDTO;
import at.ac.ase.inso.group02.rating.dto.CreateRatingDTO;
import at.ac.ase.inso.group02.rating.dto.UserRatingDTO;
import at.ac.ase.inso.group02.rating.views.RatingViews;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import at.ac.ase.inso.group02.util.pagination.PaginationParamsDTO;
import at.ac.ase.inso.group02.views.Views;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestPath;

import java.util.UUID;

/**
 * Controller for Skill-Exchanges
 */
@Path("/exchange")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ExchangeController {

    /**
     * retrieves a paginated list of all Exchange-Chats for the currently authenticated User
     * @param paginationParamsDTO pagination parameters
     * @return paginated list of all Exchange-Chats for the currently authenticated User
     */
    @GET
    @ResponseStatus(200)
    @RolesAllowed({"USER", "ADMIN"})
    @JsonView(Views.ExplicitlyTypedBrief.class)
    PaginatedQueryDTO<ExchangeChatDTO> getMyExchangeChats(@BeanParam PaginationParamsDTO paginationParamsDTO);

    /**
     * retrieves a paginated list of all Exchange-Chats for the specified User
     * @param username the username of the specified user
     * @param paginationParamsDTO pagination parameters
     * @return paginated list of all Exchange-Chats for the specified User
     */
    @GET
    @Path("/user/{username}")  // Add this path annotation
    @ResponseStatus(200)
    @RolesAllowed({"USER", "ADMIN"})
    @JsonView(Views.ExplicitlyTypedBrief.class)
    PaginatedQueryDTO<ExchangeChatDTO> getExchangeChatsByUsername(@RestPath String username, @BeanParam PaginationParamsDTO paginationParamsDTO);

    /**
     * finds the exchange chat with the provided id
     * @param exchangeChatID id of the exchange chat
     * @return the exchange chat that matches the given id
     */
    @GET
    @ResponseStatus(200)
    @RolesAllowed({"USER", "ADMIN"})
    @JsonView(Views.ExplicitlyTypedBrief.class)
    @Path("/{exchangeChatID}")
    ExchangeChatDTO getExchangeChatByID(@RestPath @Valid String exchangeChatID);

    /**
     * retrieves information about a single Skill-Exchange (that is part of some Exchange-Chat)
     * @param exchangeID ID of the Skill-Exchange
     * @return data about the matching Skill-Exchange
     */
    @GET
    @ResponseStatus(200)
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/item/{exchangeID}")
    @JsonView(Views.ExplicitlyTypedBrief.class)
    ExchangeItemDTO getExchangeItem(@RestPath Long exchangeID);

    /**
     * marks the Skill-Exchange (by ID) as completed for the authenticated user
     * @param exchangeID ID of a Skill-Exchange
     * @return the updated data of the Skill-Exchange
     */
    @POST
    @ResponseStatus(200)
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/item/{exchangeID}/complete")
    @JsonView(Views.ExplicitlyTypedBrief.class)
    ExchangeItemDTO markExchangeComplete(@RestPath Long exchangeID);

    /**
     * creates a rating for a Skill-Exchange by the authenticated user
     * @param exchangeID ID of the Skill-Exchange to rate
     * @param rating rating data provided by the user
     * @return the saved user-rating data
     */
    @POST
    @ResponseStatus(201)
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/item/{exchangeID}/rate")
    @JsonView(RatingViews.IncludeForUser.class)
    UserRatingDTO createRating(@RestPath Long exchangeID, @Valid CreateRatingDTO rating);

    /**
     * creates/initiates a new Exchange between users, containing multiple Skill-Exchanges
     * @param exchangeInitiationDTO exchange-data, see the wiki for its structure
     * @return data for an Exchange-Chat to be used for this exchange
     */
    @POST
    @ResponseStatus(201)
    @RolesAllowed({"USER", "ADMIN"})
    @JsonView(Views.ExplicitlyTypedBrief.class)
    ExchangeChatDTO initiateExchange(@Valid InitiateExchangesDTO exchangeInitiationDTO);

    /**
     * updates an Exchange between users, containing multiple Skill-Exchanges
     * @param exchangeChatID ID of the Exchange-Chat to modify
     * @param exchangeDTO new exchange-data, see the wiki for its structure
     * @return data for an Exchange-Chat to be used for this exchange
     */
    @PUT
    @ResponseStatus(201)
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/{exchangeChatID}")
    @JsonView(Views.ExplicitlyTypedBrief.class)
    ExchangeChatDTO updateExchange(@RestPath UUID exchangeChatID, @Valid InitiateExchangesDTO exchangeDTO);
}
