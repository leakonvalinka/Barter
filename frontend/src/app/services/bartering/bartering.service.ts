import {Injectable} from '@angular/core';
import {environment} from '../../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {PaginatedResults, PaginationParams, paginationToHttpQueryParams} from '../../dtos/pagination';
import {Observable} from 'rxjs';
import {ExchangeChat, ExchangeItem, InitiateExchanges} from '../../dtos/bartering';
import {CreateUserRating, UserRating} from "../../dtos/rating";
import {ChatUser, UserDetail} from '../../dtos/user';
import {UserService} from '../user/user.service';

@Injectable({
    providedIn: 'root'
})
/**
 * Service for handling Bartering Exchanges
 */
export class BarteringService {

    private exchangeEndpointUrl = `${environment.apiBaseUrl}/exchange`;
    private exchangeItemEndpointUrl = `${this.exchangeEndpointUrl}/item`;

    constructor(
        private http: HttpClient,
    ) {
    }

    /**
     * retrieves all Exchange-Chats for the current user from the backend, paginated
     */
    getMyExchangeChats(paginationParams: PaginationParams = {page: 0, pageSize: 50}): Observable<PaginatedResults<ExchangeChat>> {
        return this.http.get<PaginatedResults<ExchangeChat>>(this.exchangeEndpointUrl, {
            params: paginationToHttpQueryParams(paginationParams)
        });
    }

    /**
     * retrieves all Exchange-Chats for the specified user from the backend, paginated
     */
    getExchangeChatsByUsername(username: string, paginationParams: PaginationParams = {page: 0, pageSize: 50}): Observable<PaginatedResults<ExchangeChat>> {
        return this.http.get<PaginatedResults<ExchangeChat>>(`${this.exchangeEndpointUrl}/user/${username}`, {
            params: paginationToHttpQueryParams(paginationParams)
        });
    }

    /**
     * retrieves a single Exchange-Chat by id
     */
    getExchangeChatByID(id: string): Observable<ExchangeChat> {
      return this.http.get<ExchangeChat>(this.exchangeEndpointUrl + '/' + id);
    }

    /**
     * retrieves a single Skill-Exchange from the backend by id.
     * This skill exchange is part of an Exchange-Chat
     */
    getSkillExchangeByID(id: number): Observable<ExchangeItem> {
        return this.http.get<ExchangeItem>(this.exchangeItemEndpointUrl + '/' + id);
    }

    /**
     * makes a request to the backend to mark a Skill-Exchange (by id) as complete by the current user
     */
    markSkillExchangeComplete(id: number): Observable<ExchangeItem> {
        return this.http.post<ExchangeItem>(this.exchangeItemEndpointUrl + '/' + id + '/complete', {})
    }

    /**
     * creates a rating for a Skill-Exchange by the current user
     */
    createRatingForSkillExchange(exchangeID: number, rating: CreateUserRating): Observable<UserRating> {
        return this.http.post<UserRating>(this.exchangeItemEndpointUrl + '/' + exchangeID + '/rate', rating);
    }

    /**
     * initiates an Exchange-Chat for the given exchanges,
     * see the wiki for details
     */
    initiateExchange(exchanges: InitiateExchanges): Observable<ExchangeChat> {
        return this.http.post<ExchangeChat>(this.exchangeEndpointUrl, exchanges);
    }

    /**
     * updates the Skill-Exchanges of an existing Exchange-Chat
     */
    updateExchange(exchangeChatID: string, newExchanges: InitiateExchanges): Observable<ExchangeChat> {
        return this.http.put<ExchangeChat>(this.exchangeEndpointUrl + '/' + exchangeChatID, newExchanges);
    }
}
