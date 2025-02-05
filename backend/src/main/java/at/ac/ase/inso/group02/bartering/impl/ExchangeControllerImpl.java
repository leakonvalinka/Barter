package at.ac.ase.inso.group02.bartering.impl;

import at.ac.ase.inso.group02.bartering.ExchangeController;
import at.ac.ase.inso.group02.bartering.ExchangeService;
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
import io.quarkus.logging.Log;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class ExchangeControllerImpl implements ExchangeController {

    private final ExchangeService exchangeService;

    @Override
    @JsonView(Views.ExplicitlyTypedBrief.class)
    public PaginatedQueryDTO<ExchangeChatDTO> getMyExchangeChats(PaginationParamsDTO paginationParamsDTO) {
        return exchangeService.getExchangeChatsForCurrentUser(paginationParamsDTO);
    }

    @Override
    @JsonView(Views.ExplicitlyTypedBrief.class)
    public PaginatedQueryDTO<ExchangeChatDTO> getExchangeChatsByUsername(String username, PaginationParamsDTO paginationParamsDTO) {
        Log.infov("Fetching exchange chats for user {0} with pagination params: {1}", username, paginationParamsDTO);
        return exchangeService.getExchangeChatsByUsername(username, paginationParamsDTO);
    }

    @Override
    @JsonView(Views.ExplicitlyTypedBrief.class)
    public ExchangeChatDTO getExchangeChatByID(String exchangeChatID) {
        Log.infov("Fetching exchange chat: {0}", exchangeChatID);
        return exchangeService.getExchangeChat(exchangeChatID);
    }

    @Override
    @JsonView(Views.ExplicitlyTypedBrief.class)
    public ExchangeItemDTO getExchangeItem(Long exchangeID) {
        Log.infov("Fetching exchange item: {0}", exchangeID);
        return exchangeService.getExchangeItem(exchangeID);
    }

    @Override
    @JsonView(Views.ExplicitlyTypedBrief.class)
    public ExchangeItemDTO markExchangeComplete(Long exchangeID) {
        Log.infov("Completing exchange: {0}", exchangeID);
        return exchangeService.finalizeExchange(exchangeID);
    }

    @Override
    @JsonView(RatingViews.IncludeForUser.class)
    public UserRatingDTO createRating(Long exchangeID, CreateRatingDTO rating) {
        Log.infov("Creating rating for exchange {0} with values: {1}",exchangeID, rating);
        return exchangeService.createRatingForExchange(exchangeID, rating);
    }

    @Override
    @JsonView(Views.ExplicitlyTypedBrief.class)
    public ExchangeChatDTO initiateExchange(@Valid InitiateExchangesDTO exchangeInitiationDTO) {
        Log.infov("Initiating new exchange: {0}", exchangeInitiationDTO);
        return exchangeService.initiateExchange(exchangeInitiationDTO);
    }

    @Override
    @JsonView(Views.ExplicitlyTypedBrief.class)
    public ExchangeChatDTO updateExchange(UUID exchangeChatID, @Valid InitiateExchangesDTO exchangeDTO) {
        Log.infov("Updating exchange with chat ID {0} with values: {1}", exchangeChatID, exchangeDTO);
        return exchangeService.updateExchange(exchangeChatID, exchangeDTO);
    }
}
