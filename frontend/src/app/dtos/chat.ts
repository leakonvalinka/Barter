import { UserDetail } from './user';

export interface WSTicketDTO {
  ticketUUID: string;
  expires: string;
}

export interface NewChatMessageDTO {
  content: string;
}

export enum MessageReadState {
  UNSEEN,
  SEEN
}

export interface ChatMessageDTO {
  id: string,
  exchangeID: string,
  content: string,
  exchangeChanged: boolean, // if true, indicates that the exchange was modified and details should be refreshed
  author: UserDetail, // should be UserInfo, but that doesn't exist in the frontend
  timestamp: string,
  readState: MessageReadState | null // null when message isn't authored by the current user
}

export interface ChatNotificationDTO{
  numberOfMessages: number
}
