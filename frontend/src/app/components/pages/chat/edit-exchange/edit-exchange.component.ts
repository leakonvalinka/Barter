import {Component, EventEmitter, Input, Output, signal} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {CreateExchange, ExchangeChat, InitiateExchanges} from '../../../../dtos/bartering';
import {BarteringService} from '../../../../services/bartering/bartering.service';
import {ChatService} from '../../../../services/chat/chat.service';
import {Skill, SkillOffer} from '../../../../dtos/skill';
import {UserService} from '../../../../services/user/user.service';
import {UserDetail} from '../../../../dtos/user';
import {SkillService} from '../../../../services/skill/skill.service';
import {NewChatMessageDTO} from '../../../../dtos/chat';

@Component({
  selector: 'app-edit-exchange',
  imports: [
    ReactiveFormsModule,
    CommonModule
  ],
  templateUrl: './edit-exchange.component.html',
  standalone: true,
  styleUrl: './edit-exchange.component.scss'
})
export class EditExchangeComponent {
  @Input() exchangeId: string = '00000000-0000-0000-0000-000000000003';
  @Output() exchangeConfirmed = new EventEmitter<any>();
  @Output() closeDialog = new EventEmitter<void>();

  isLoading = signal<boolean>(true);

  exchangeChanged: boolean = false;

  selectedYourSkills: Skill[] = [];
  selectedTheirSkills: Skill[] = [];
  yourSkills: Skill[] = [];
  theirSkills: Skill[] = [];
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

  constructor(private barteringService: BarteringService,
              private chatService: ChatService,
              private userService: UserService,
              private skillService: SkillService,
  ) {
  }

  ngOnInit() {
    this.isLoading.set(true);
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
        this.extractSelectedSkills();
        this.extractUserSkills();

        this.isLoading.set(false);
      });
    });

  }

  extractUserSkills() {
    for (const participant of this.participants) {
      if (!participant.skillOffers) {
        this.userService.getDetailedUser(participant.username).subscribe(detailedUser => {
          if (detailedUser.skillOffers) {
            const offers = detailedUser.skillOffers.filter(skill => !this.isSelectedSkill(skill));
            const currentUser = this.userService.isCurrentUser(detailedUser.username);

            if (currentUser) {
              this.yourSkills = [...this.yourSkills, ...offers];
            } else {
              this.theirSkills = [...this.theirSkills, ...offers];
            }
          }
        });
      }
    }
  }

  isSelectedSkill(skill: SkillOffer): boolean {
    return this.selectedTheirSkills.includes(skill);
  }

  extractSelectedSkills() {
    for (const item of this.exchangeChat.exchanges) {
      if (item.exchangedSkill && this.isCurrentUser(item.exchangedSkill)) {
        this.selectedYourSkills.push(item.exchangedSkill);
      }
      if (item.exchangedSkill && !this.isCurrentUser(item.exchangedSkill)) {
        this.selectedTheirSkills.push(item.exchangedSkill);
      }
      if (item.exchangedSkillCounterpart && this.isCurrentUser(item.exchangedSkillCounterpart)) {
        this.selectedYourSkills.push(item.exchangedSkillCounterpart);
      }
      if (item.exchangedSkillCounterpart && !this.isCurrentUser(item.exchangedSkillCounterpart)) {
        this.selectedTheirSkills.push(item.exchangedSkillCounterpart);
      }
    }
  }

  isCurrentUser(skill: Skill): boolean {
    return this.userService.isCurrentUser(skill.byUser.username);
  }

  getCurrentUserIndex(): number {
    return this.participants.findIndex(participant =>
      this.userService.isCurrentUser(participant.username)
    );
  }

  getOtherUserIndex(): number {
    return this.participants.findIndex(participant =>
      !this.userService.isCurrentUser(participant.username)
    );
  }

  selectYourSkill(skill: Skill) {
    this.yourSkills = this.yourSkills.filter(s => skill.id !== s.id);
    this.selectedYourSkills.push(skill);
    this.exchangeChanged = true;
  }

  deselectYourSkill(skill: Skill) {
    this.selectedYourSkills = this.selectedYourSkills.filter(s => skill.id !== s.id);
    this.yourSkills.push(skill);
    this.exchangeChanged = true;
  }

  selectTheirSkill(skill: Skill) {
    this.theirSkills = this.theirSkills.filter(s => skill.id !== s.id);
    this.selectedTheirSkills.push(skill);
    this.exchangeChanged = true;
  }

  deselectTheirSkill(skill: Skill) {
    this.selectedTheirSkills = this.selectedTheirSkills.filter(s => skill.id !== s.id);
    this.theirSkills.push(skill);
    this.exchangeChanged = true;
  }

  confirmExchange() {
    if (this.selectedYourSkills && this.selectedTheirSkills) {
      const newExchanges = this.createInitialExchange();
      this.barteringService.updateExchange(this.exchangeId, newExchanges).subscribe(() => {
        console.log('exchange was confirmed')
        this.exchangeConfirmed.emit();
        this.cancel();
      });
    } //ERROR HANDLING
  }

  createInitialExchange(): InitiateExchanges {
    const newExchanges = this.createExchanges();
    const newMessage: NewChatMessageDTO = {
      content: 'Hi, I changed something for our Exchange. To view and change it press the Edit Exchanges Button, to accept just respond to this message!'
    }

    return {
      exchanges: newExchanges,
      chatMessage: newMessage,
    }
  }

  createExchanges(): CreateExchange[] {
    let exchanges: CreateExchange[] = [];

    //Add their skills first
    const currentUser = this.getCurrentUserIndex();
    for (const skill of this.selectedTheirSkills) {
      const exchange: CreateExchange = {
        skillID: skill.id,
        forUser: this.participants[currentUser].username
      }
      exchanges.push(exchange);
    }

    //Add own Skills
    const otherUser = this.getOtherUserIndex();
    for (const skill of this.selectedYourSkills) {
      const exchange: CreateExchange = {
        skillID: skill.id,
        forUser: this.participants[otherUser].username
      }
      exchanges.push(exchange);
    }

    return exchanges;
  }

  cancel() {
    this.closeDialog.emit();
  }
}
