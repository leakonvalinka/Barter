import {Component, OnInit, signal} from '@angular/core';
import {Router} from '@angular/router';
import {CommonModule, DatePipe} from '@angular/common';
import {ChatService} from '../../../../services/chat/chat.service';
import {BarteringService} from '../../../../services/bartering/bartering.service';
import {ChatUser, UserDetail} from '../../../../dtos/user';
import {UserService} from '../../../../services/user/user.service';
import {ExchangeChat} from '../../../../dtos/bartering';
import {ChatMessageDTO} from '../../../../dtos/chat';
import {environment} from '../../../../../environments/environment';
import {ToastrService} from 'ngx-toastr';
import {SkeletonComponent} from '../../../util/skeleton/skeleton.component';

interface ChatPreview {
  id: string;
  username: string;
  lastMessage: string;
  timestamp: string;
  profilePicture: string | undefined;
  theirTurn: boolean;
}

@Component({
  selector: 'app-chat-overview',
  imports: [
    DatePipe,
    CommonModule,
    SkeletonComponent
  ],
  templateUrl: './chat-overview.component.html',
  standalone: true,
  styleUrl: './chat-overview.component.scss'
})
export class ChatOverviewComponent implements OnInit {
  openChats = signal<ChatPreview[]>([]);
  ratedChats = signal<ChatPreview[]>([]);
  isLoading = signal<boolean>(true);
  isArchiveOpen = signal<boolean>(false);

  constructor(private router: Router,
              private chatService: ChatService,
              private barteringService: BarteringService,
              private userService: UserService,
              private toastr: ToastrService,
  ) {
  }

  toggleArchive(): void {
    this.isArchiveOpen.set(!this.isArchiveOpen());
  }

  async ngOnInit(): Promise<void> {
    this.getExchanges();

    this.chatService.connectWebSocket();
    this.chatService.onNewMessage((message: ChatMessageDTO) => {
      this.openChats.set(this.handleChat(message, this.openChats()));
      this.ratedChats.set(this.handleChat(message, this.ratedChats()));
    });
  }

  handleChat(message: ChatMessageDTO, chats: ChatPreview[]): ChatPreview[] {
    const currentChats = chats;
    const updatedChats = currentChats.map(chat => {
      if (chat.id === message.exchangeID && message.timestamp > chat.timestamp) {
        return {
          ...chat,
          lastMessage: message.content,
          timestamp: message.timestamp,
          theirTurn: !this.isAuthor(message)
        };
      }
      return chat;
    });
    return updatedChats;
  }

  navigateToChat(chatId: string): void {
    this.router.navigate(['/chat', chatId]);
  }

  getChatParticipants(chat: ExchangeChat): ChatUser[] {
    let participants: ChatUser[] = [];
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

    //removing the current user
    users = users.filter(user => !this.userService.isCurrentUser(user.username));

    //mapping them to ChatUser
    for (const user of users) {
      const chatUser = {
        username: user.username,
        displayName: user.displayName,
        bio: user.bio,
        profilePicture: user.profilePicture,
      };

      participants.push(chatUser);
    }
    return participants;
  }

  private getExchanges() {
    this.isLoading.set(true);
    this.barteringService.getMyExchangeChats().subscribe(
      chats => {
        const openChatPreviews: ChatPreview[] = [];
        const ratedChatPreviews: ChatPreview[] = [];
        //Map each chat to the ChatPreviewDTO
        for (const chat of chats.items) {
          if (chat.exchanges.length <= 0) { // This can happen, if a user from this chat got banned.
            continue;
          }
          const participants = this.getChatParticipants(chat);

          if (participants[0] && participants[0] !== undefined) {
            const preview: ChatPreview = {
              id: chat.id,
              username: participants[0].displayName ? participants[0].displayName : participants[0].username,
              lastMessage: chat.mostRecentMessage !== null ? chat.mostRecentMessage.content : 'Click to start your chat',
              timestamp: chat.mostRecentMessage !== null ? chat.mostRecentMessage.timestamp : '',
              profilePicture: participants[0].profilePicture ? `${environment.apiBaseUrl}/images/${participants[0].profilePicture}` : `resources/profile_icon.png`,
              theirTurn: !this.isAuthor(chat.mostRecentMessage),
            };

            if (this.userService.isCurrentUser(chat.exchanges[0].initiator.username) && chat.exchanges[0].initiatorRating !== null) {
              ratedChatPreviews.push(preview);
            } else if (!this.userService.isCurrentUser(chat.exchanges[0].initiator.username) && chat.exchanges[0].responderRating !== null) {
              ratedChatPreviews.push(preview);
            } else {
              openChatPreviews.push(preview);
            }

          } else {
            console.error('Participants are undefined or empty for', chat.id);
            this.toastr.error('Participants are undefined or empty for this chat');
          }
        }
        this.openChats.set(openChatPreviews);
        this.ratedChats.set(ratedChatPreviews);
        if (this.openChats().length <= 0) {
          this.toggleArchive();
        }
        this.isLoading.set(false);
      },
      error => {
        console.error('Error fetching chats:', error);
        this.toastr.error('Failed to load chats');
        this.isLoading.set(false);
      });
  }

  isAuthor(msg: ChatMessageDTO): boolean {
    return this.userService.isCurrentUser(msg.author.username);
  }

  getFormattedDate(timestamp: string): string {
    const messageDate = new Date(timestamp);
    const today = new Date();
    const yesterday = new Date();
    yesterday.setDate(today.getDate() - 1);

    // Format to only compare day, month, and year
    const isToday =
      messageDate.getDate() === today.getDate() &&
      messageDate.getMonth() === today.getMonth() &&
      messageDate.getFullYear() === today.getFullYear();

    const isYesterday =
      messageDate.getDate() === yesterday.getDate() &&
      messageDate.getMonth() === yesterday.getMonth() &&
      messageDate.getFullYear() === yesterday.getFullYear();

    if (isToday) {
      return 'Today';
    } else if (isYesterday) {
      return 'Yesterday';
    } else {
      return messageDate.toLocaleDateString('en-GB'); // Displays in dd/mm/yyyy format
    }
  }
}
