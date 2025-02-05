import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Skill, SkillDemand, SkillOffer} from '../../../../../dtos/skill';
import {UserDetail} from '../../../../../dtos/user';
import {ExchangeChat, ExchangeItem} from '../../../../../dtos/bartering';
import {BarteringService} from '../../../../../services/bartering/bartering.service';
import {ChatService} from '../../../../../services/chat/chat.service';
import {UserService} from '../../../../../services/user/user.service';
import {NewChatMessageDTO} from '../../../../../dtos/chat';
import {NgForOf, NgIf} from '@angular/common';
import {Router} from '@angular/router';

@Component({
  selector: 'app-exchange-overview',
  imports: [
    NgForOf,
    NgIf
  ],
  templateUrl: './exchange-overview.component.html',
  standalone: true,
  styleUrl: './exchange-overview.component.scss'
})
export class ExchangeOverviewComponent implements OnInit {
  @Input() exchangeId: string = '';
  //@Input() alreadyCompleted: boolean; maybe needed for marking the chat complete? idk
  @Output() exchangeComplete = new EventEmitter<void>();
  @Output() closeDialog = new EventEmitter<void>();

  theirDemands: Skill[] = [];
  yourDemands: Skill[] = [];
  yourOffers: Skill[] = [];
  theirOffers: Skill[] = [];
  participants: UserDetail[] = [];

  exchangeChat: ExchangeChat = {
    id: '',
    exchanges: [],
    initiator: {
      id: 0,
      email: '',
      username: '',
      displayName: '',
      bio: '',
      profilePicture: '',
      location: {
        street: '',
        streetNumber: '',
        city: '',
        postalCode: 0,
        country: '',
        homeLocation: {
          type: '',
          coordinates: [],
        },
      },
      skillDemands: [],
      skillOffers: [],
    },
    confirmationResponsePending: false,
    numberOfUnseenMessages: 0,
    mostRecentMessage: {
      id: '',
      exchangeID: '',
      content: '',
      exchangeChanged: false,
      author: {
        id: 0,
        email: '',
        username: '',
        displayName: '',
        bio: '',
        profilePicture: '',
        location: {
          street: '',
          streetNumber: '',
          city: '',
          postalCode: 0,
          country: '',
          homeLocation: {
            type: '',
            coordinates: [],
          },
        },
        skillDemands: [],
        skillOffers: [],
      },
      timestamp: '',
      readState: null,
    },
  };

  constructor(private router: Router,
              private barteringService: BarteringService,
              private chatService: ChatService,
              private userService: UserService,
  ) {
  }

  ngOnInit() {
    this.barteringService.getExchangeChatByID(this.exchangeId).subscribe(exchangeChat => {
      this.exchangeChat = exchangeChat;

      this.barteringService.getExchangeChatByID(this.exchangeId).subscribe(chat => {
        let users: UserDetail[] = [];
        //getting every user involved
        for (const exchange of chat.exchanges) {
          if (!users.includes(exchange.initiator)) {
            users.push(exchange.initiator);
          }
          if (!users.includes(exchange.exchangedSkill.byUser)) {
            users.push(exchange.exchangedSkill.byUser);
          }
          if (exchange.exchangedSkillCounterpart && !users.includes(exchange.exchangedSkillCounterpart.byUser)) {
            users.push(exchange.exchangedSkillCounterpart.byUser);
          }
        }
        this.participants = users;
        this.extractSkills();
      });
    });
  }

  extractSkills() {
    for (const item of this.exchangeChat.exchanges) {
      if (item.exchangedSkill.type === 'demand') {
        this.addDemands(item.exchangedSkill);
      }
      if (item.exchangedSkill.type === 'offer') {
        this.addOffers(item.exchangedSkill);
      }
      if (item.exchangedSkillCounterpart && item.exchangedSkillCounterpart.type === 'demand') {
        this.addDemands(item.exchangedSkillCounterpart);
      }
      if (item.exchangedSkillCounterpart && item.exchangedSkillCounterpart.type === 'offer') {
        this.addOffers(item.exchangedSkillCounterpart);
      }
    }
  }

  addDemands(skill: SkillDemand) {
    const currentUser = this.isCurrentUser(skill);
    if (currentUser) {
      this.yourDemands.push(skill);
    } else {
      this.theirDemands.push(skill);
    }
  }

  addOffers(skill: SkillOffer) {
    const currentUser = this.isCurrentUser(skill);
    if (currentUser) {
      this.yourOffers.push(skill);
    } else {
      this.theirOffers.push(skill);
    }
  }

  isCurrentUser(skill: Skill): boolean {
    return this.userService.isCurrentUser(skill.byUser.username);
  }

  truncateTitle(s: string, maxLength: number = 30): string {
    if (!s) return '';
    return s.length > maxLength ? s.substring(0, maxLength) + '...' : s;
  }

  showYourExchangeSkill(skill: Skill | null): string {
    return skill && this.isCurrentUser(skill) ? this.truncateTitle(skill.title) : 'None';
  }

  showTheirExchangeSkill(skill: Skill | null): string {
    return skill && !this.isCurrentUser(skill) ? this.truncateTitle(skill.title) : 'None';
  }

  showDemandOrOffer(skill: Skill | null): string {
    if (skill) {
      if (skill.type === "demand") {
        return 'Demand';
      }
      if (skill.type === "offer") {
        return 'Offer';
      }
    }
    return '    ';
  }

  currentUserHasCounterPart(exchangeItem: ExchangeItem): boolean {
    if (exchangeItem.exchangedSkillCounterpart) {
      return this.isCurrentUser(exchangeItem.exchangedSkillCounterpart);
    }
    return false;
  }

  otherUserHasCounterPart(exchangeItem: ExchangeItem): boolean {
    if (exchangeItem.exchangedSkillCounterpart) {
      return !this.isCurrentUser(exchangeItem.exchangedSkillCounterpart);
    }
    return false;
  }

  currentUserHasSkill(exchangeItem: ExchangeItem): boolean {
    return this.isCurrentUser(exchangeItem.exchangedSkill);
  }

  back() {
    this.closeDialog.emit();
  }

  navigateToSkill(skill: SkillDemand | SkillOffer | null) {
    if (skill) {
      if (skill.type === "demand") {
        this.router.navigate(['/demands', skill.id]);
        this.back();
      } else if (skill.type === "offer") {
        this.router.navigate(['/offers', skill.id]);
        this.back();
      }
    } else {
      return;
    }
  }

  markAsCompleted() {
    for (const item of this.exchangeChat.exchanges) {
      this.barteringService.markSkillExchangeComplete(item.id);
    }

    const newMessage: NewChatMessageDTO = {
      content: 'Hi, I marked this Exchange as complete, to end the Exchange please view it and mark it as complete.'
    };

    this.chatService.sendMessage(this.exchangeId, newMessage).subscribe(msg => {
      // Emit the first exchange ID for rating
      if (this.exchangeChat.exchanges.length > 0) {
        this.exchangeComplete.emit();
      }
    });
  }
}
