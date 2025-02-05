import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {Observable} from 'rxjs';
import {ChatMessageDTO, ChatNotificationDTO, NewChatMessageDTO, WSTicketDTO} from '../../dtos/chat';
import {PaginatedResults, PaginationParams, paginationToHttpQueryParams} from '../../dtos/pagination';

@Injectable({
  providedIn: 'root'
})

export class ChatService {

  private chatEndpointURL = `${environment.apiBaseUrl}/chat`;
  private ticketEndpointUrl = `${this.chatEndpointURL}/ticket`;
  private wsEndpointUrl = `${environment.wsBaseUrl}/chat/ws`;

  private socket: WebSocket | undefined = undefined;

  private newMessageFunctions: ((message: ChatMessageDTO) => void)[] = [];
  private onConnectFunctions: (() => void)[] = [];

  constructor(
    private http: HttpClient
  ) {
  }

  /**
   * initiates the connection to the WebSocket,
   * should ideally only be called once
   */
  connectWebSocket() {
    if(this.socket && this.socket.readyState === WebSocket.OPEN){
      // don't create new WebSocket if already initialized
      return;
    }
    this.getTicket().subscribe(ticket => {
      this.socket = new WebSocket(this.wsEndpointUrl + "/" + ticket.ticketUUID);

      this.socket.onopen = () => {
        console.log("Connected to the WebSocket");
        for (const onConnectFunction of this.onConnectFunctions) {
          onConnectFunction();
        }
      };

      this.applyNewMessageFunctions();

      this.socket.onclose = function (m) {
        console.log("Chat WebSocket closed. Reason: " + m.reason)
      }
    }) //todo: implement if already initiated do nothing
  }

  /**
   * will execute onMessageFunction each time a new message is received via the WebSocket
   *
   * Note that a message with the same ID may be received multiple times, indicating an update
   * (e.g. when the other user has read your message, your own message will be received again,
   * but this time with message.readState=SEEN)
   * @param onMessageFunction a function that consumes the newly received message
   */
  onNewMessage(onMessageFunction: (message: ChatMessageDTO) => void) {
    this.newMessageFunctions.push(onMessageFunction);
    this.applyNewMessageFunctions();
  }

  private applyNewMessageFunctions() {
    if (this.socket) {
      this.socket.onmessage = (m: { data: string; }) => {
        const chatMessage = JSON.parse(m.data);
        for (const newMessageFunction of this.newMessageFunctions) {
          newMessageFunction(chatMessage);
        }
      }
    }
  }

  /**
   * will execute onConnectFunction after the WebSocket connection has been established,
   * or immediately if already connected
   * @param onConnectFunction a function to execute after the websocket has connected
   */
  executeAfterConnect(onConnectFunction: () => void){
    if(this.socket && this.socket.readyState === WebSocket.OPEN){
      onConnectFunction();
    } else {
      this.onConnectFunctions.push(onConnectFunction);
    }
  }

  /**
   * sends a new message to the Server
   * @param exchangeID exchange for which the message is intended (will be sent to all participants of the exchange)
   * @param chatMessage message details
   */
  sendMessage(exchangeID: string, chatMessage: NewChatMessageDTO): Observable<ChatMessageDTO> {
    return this.http.post<ChatMessageDTO>(`${this.chatEndpointURL}/${exchangeID}`, chatMessage);
  }

  /**
   * explicitly tells the server that a single message has been read by the user
   * @param message message that the user definitely read
   */
  markMessageAsSeen(message: ChatMessageDTO) {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify({chatMessageID: message.id}));
    }
  }

  /**
   * retrieves the last <count> messages for an exchange that were posted before the message with id <beforeMessageUUID>
   * This implicitly tells the server that the retrieved messages have been read!
   * @param exchangeID
   * @param count
   * @param beforeMessageUUID
   */
  getMessagesForExchange(exchangeID: string, count: number = 20, beforeMessageUUID: string | undefined = undefined): Observable<ChatMessageDTO[]>{
    let params = new HttpParams()
      .set('count', count.toString());

    if (beforeMessageUUID){
      params = params.set('beforeMessageUUID', beforeMessageUUID);
    }

    return this.http.get<ChatMessageDTO[]>(`${this.chatEndpointURL}/${exchangeID}`, {
      params: params
    })
  }

  /**
   * retrieves all unseen (/unread) messages for the current user, paginated
   * This does *not* mark these messages as seen
   * @param paginationParams pagination parameters
   */
  getUnseenMessages(paginationParams: PaginationParams = {page: 0, pageSize: 50}): Observable<PaginatedResults<ChatMessageDTO>>{
    return this.http.get<PaginatedResults<ChatMessageDTO>>(this.chatEndpointURL, {
      params: paginationToHttpQueryParams(paginationParams)
    })
  }

  /**
   * retrieves a ticket to authenticate a new WebSocket connection
   */
  private getTicket(): Observable<WSTicketDTO> {
    return this.http.post<WSTicketDTO>(this.ticketEndpointUrl, {});
  }

  /**
   * retrieves the number of new messages that the user was not yet notified for
   * (These messages will be marked as 'notified' and are excluded from subsequent requests)
   */
  getNotifications(): Observable<ChatNotificationDTO>{
    return this.http.get<ChatNotificationDTO>(`${this.chatEndpointURL}/notifications`)
  }
}
