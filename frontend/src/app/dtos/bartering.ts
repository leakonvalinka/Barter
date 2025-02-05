import {Skill} from './skill';
import {UserDetail} from './user';
import {UserRating} from './rating';
import {ChatMessageDTO, NewChatMessageDTO} from './chat';

/**
 * represents the creation of a single Skill-Exchange
 */
export interface CreateExchange {
  skillID: number;
  skillCounterPartID?: number;
  forUser?: string;
}

/**
 * represents the initiation of a bartering-exchange containing multiple Skill-Exchange "proposals"
 */
export interface InitiateExchanges {
  exchanges: CreateExchange[];
  chatMessage: NewChatMessageDTO
}

/**
 * represents an exchange for a single Skill
 */
export interface ExchangeItem {
  id: number;
  exchangedSkill: Skill;
  exchangedSkillCounterpart: Skill | null;
  initiator: UserDetail;
  numberOfExchanges: number;

  firstExchangeAt: string;
  lastExchangeAt: string;

  ratable: boolean;
  initiatorMarkedComplete: boolean;
  initiatorRating: UserRating | null;
  responderMarkedComplete: boolean;
  responderRating: UserRating | null;
}

/**
 * represents an exchange (-chat) for the exchange of multiple Skills
 */
export interface ExchangeChat {
  id: string;
  exchanges: ExchangeItem[];
  initiator: UserDetail;
  confirmationResponsePending: boolean;
  numberOfUnseenMessages: number;
  mostRecentMessage: ChatMessageDTO;
}
