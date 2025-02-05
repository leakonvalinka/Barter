import {Component, OnInit, ViewChild, ElementRef} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {ChatService} from '../../../../services/chat/chat.service';
import {BarteringService} from '../../../../services/bartering/bartering.service';
import {ChatMessageDTO, NewChatMessageDTO} from '../../../../dtos/chat';
import {UserService} from '../../../../services/user/user.service';
import {ChatUser, UserDetail} from '../../../../dtos/user';
import {environment} from '../../../../../environments/environment';
import {NgpDialog, NgpDialogOverlay, NgpDialogTrigger} from 'ng-primitives/dialog';
import {ExchangeOverviewComponent} from '../exchange-overview/exchange-overview/exchange-overview.component';
import { RatingComponent } from '../../rating/rating.component';
import { ToastrService } from 'ngx-toastr';
import { ConfirmationDialogService } from '../../../../services/dialog/confirmation-dialog.service';
import { AdminService } from '../../../../services/admin/admin.service';
import { ExchangeChat } from '../../../../dtos/bartering';
import { lastValueFrom } from 'rxjs';

@Component({
  selector: 'app-chat-detail',
  imports: [
    FormsModule,
    CommonModule,
    NgpDialogOverlay,
    NgpDialog,
    NgpDialogTrigger,
    ExchangeOverviewComponent,
    RatingComponent
],
  templateUrl: './chat-detail.component.html',
  standalone: true,
  styleUrl: './chat-detail.component.scss'
})
export class ChatDetailComponent implements OnInit {
  @ViewChild('dialog') dialog!: NgpDialog;
  @ViewChild('dialogTrigger') dialogTrigger!: ElementRef<HTMLElement>;
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;

  messages: ChatMessageDTO[] = [];
  newMessage = '';
  chatId: string = '';
  participants: ChatUser[] = [];
  participantsUsername: string[] = [];

  isLoading: boolean = false;
  loadingButton: boolean = false;

  dialogState: 'exchange' | 'rating' | 'report' = 'exchange';
  currentExchangeId: number = 0;

  isChatDisabled: boolean = false;
  showOverlay: boolean = false;
  chat: ExchangeChat | null = null;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private chatService: ChatService,
              private barteringService: BarteringService,
              private userService: UserService,
              private toastr: ToastrService, 
              private confirmationDialog: ConfirmationDialogService,
              private adminService: AdminService) {
  }

  async ngOnInit(): Promise<void> {
    this.route.paramMap.subscribe(params => {
      this.chatId = params.get("id") ?? '';

      this.getChatParticipants();
      this.loadMessages();

      this.chatService.connectWebSocket();
      this.chatService.onNewMessage((message: ChatMessageDTO) => {
        this.prependMessage(message);
        this.chatService.markMessageAsSeen(message);
      });
    });
  }

  isAuthor(msg: ChatMessageDTO): boolean {
    return this.userService.isCurrentUser(msg.author.username);
  }

  onScroll(event: Event): void {
    const element = event.target as HTMLElement;

    if (element.scrollTop === 0 && !this.isLoading) {
      this.loadingButton = true;
    }
  }

  navigateToProfile(username: string): void {
    this.router.navigate(['/profile', username]);
  }

  prependMessage(msg: ChatMessageDTO): void {
    const existsAlready = this.messages.some(m => m.id === msg.id);
    if (existsAlready) {
      this.messages = this.messages.map(m => m.id === msg.id ? msg : m);
    } else if (msg?.timestamp > this.messages[0]?.timestamp) {
      this.messages = [msg, ...this.messages];
    }
    setTimeout(() => {
      this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
    }, 100);
  }

  //ensures no duplicates
  addMessage(msg: ChatMessageDTO): void {
    const existsAlready = this.messages.some(m => m.id === msg.id);
    if (!existsAlready) {
      this.messages.push(msg);
    }
  }

  loadMessages(): void {
    // this is how you get all (the first 20) chat-messages for an exchange:
    this.chatService.getMessagesForExchange(this.chatId).subscribe(messages => {
      this.messages = messages;
      for (const message of messages) {
        this.chatService.markMessageAsSeen(message);
      }
    });
    setTimeout(() => {
      console.log("Scrolling to bottom");
      this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
    }, 300);
  }

  loadOlderMessages(): void {
    const lastMessageId = this.messages.length > 0 ? this.messages.at(this.messages.length - 1)?.id : undefined;
    if (lastMessageId) {
      this.chatService.getMessagesForExchange(this.chatId, 20, lastMessageId).subscribe(olderMessages => {
        for (const olderMessage of olderMessages) {
          this.addMessage(olderMessage);
        }
        this.loadingButton = false;
      });
    }
  }

  handleSendMessage(content: string): void {
    const newMessage: NewChatMessageDTO = {
      content: content.trim()
    };

    this.chatService.sendMessage(this.chatId, newMessage).subscribe(msg => {
      this.newMessage = '';
    });
  }

  handleKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      // Prevent the default behavior (line break)
      event.preventDefault();

      // Send the message
      this.handleSendMessage(this.newMessage);
    }
  }

  async handleExchangeComplete() {
    if (this.chat?.exchanges !== undefined) {
      for (const exchange of this.chat?.exchanges) {
        await lastValueFrom(this.barteringService.markSkillExchangeComplete(exchange.id));
      }
      this.toastr.success("Successfully marked the exchange as complete.");
      this.dialogState = 'rating';
    } else {
      this.toastr.error("No exchange items to complete in this chat.");
      console.log("This chat has no exchange items to complete or rate: " + this.chat);
    }
  }

  handleRatingSubmitted() {
    this.dialogState = 'exchange';
  }

  resetDialogState() {
    this.dialogState = 'exchange';
  }

  getChatParticipants(): void {
    this.barteringService.getExchangeChatByID(this.chatId).subscribe(chat => {
      this.currentExchangeId = chat.exchanges[0].id;
      this.chat = chat;
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
          profilePicture: user.profilePicture ? `${environment.apiBaseUrl}/images/${user.profilePicture}` : `resources/profile_icon.png`
        };

        this.participants.push(chatUser);
        this.participantsUsername.push(chatUser.username);
      }

      // Check if chat is already completed and rates. If YES, then disable the chat.
      if (this.userService.isCurrentUser(chat.exchanges[0].initiator.username) && chat.exchanges[0].initiatorRating !== null) {
        this.isChatDisabled = true;
        this.showOverlay = true;
      } else if (!this.userService.isCurrentUser(chat.exchanges[0].initiator.username) && chat.exchanges[0].responderRating !== null) {
        this.isChatDisabled = true;
        this.showOverlay = true;
      }
    });
  }

  openRatingDialog() {
    this.dialogState = 'rating';
    this.barteringService.getSkillExchangeByID(this.currentExchangeId).subscribe({
      next: (exchangeItem) => {
        if (exchangeItem.ratable) {
          this.dialogTrigger.nativeElement.click();
        } else {
          this.toastr.info("This exchange is not ratable yet. Please wait three days or until all participants have marked it as completed.")
        }
      }
    });
  }

  async openReportDialog() {
    const confirmed = await this.confirmationDialog.confirm({
      title: 'Report User?',
      message: 'Are you sure you want to report this user? This action cannot be undone.',
      confirmText: 'Yes, Report',
      cancelText: 'Cancel',
      showTextField: true,
      textFieldLabel: 'Reason for reporting',
      textFieldPlaceholder: 'Please enter the reason for reporting this user'
    });

    if (confirmed.confirmed) {
      this.adminService.reportUser(this.participantsUsername[0], confirmed.text ?? "").subscribe({
        next: () => {
          this.toastr.success('User reported successfully');
          this.router.navigate(['/chat']);
        },
        error: (error) => {
          console.error('Error reporting user:', error);
          this.toastr.error('Error reporting user');
        }
      });
    }
  }

}
