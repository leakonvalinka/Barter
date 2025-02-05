import { Component, effect, signal } from "@angular/core";
import { NavigationComponent } from "../../util/navigation/navigation.component";
import { CommonModule } from "@angular/common";
import { RouterOutlet } from "@angular/router";
import { AuthenticationService } from "../../../services/auth/auth.service";
import { environment } from "../../../../environments/environment";
import { ConfirmationDialogService } from "../../../services/dialog/confirmation-dialog.service";

@Component({
  selector: 'app-main-layout',
  imports: [RouterOutlet, NavigationComponent, CommonModule],
  styleUrls: ['./main-layout.component.scss'],
  template: `
    <app-navigation></app-navigation>
    <!-- <div class="notification-permission" *ngIf="showNotificationPermission()">
      <button (click)="promptNotificationPermission()">Allow notifications</button>
    </div> -->
    <div class="content">
      <router-outlet></router-outlet>
    </div>
  `
})
export class MainLayoutComponent {
  showNotificationPermission = signal(false);
  constructor(private authService: AuthenticationService, private confirmationDialog: ConfirmationDialogService) {
    effect(async () => {
      console.log(this.showNotificationPermission())
      if (this.showNotificationPermission()) {
        await this.confirmationDialog.confirm({
          title: 'Allow Notifications',
          message: 'If you want to receive notifications via the browser, you need to allow notifications. After you click ok, you will be prompted to grant permission by your browser',
          confirmText: 'Ok',
          cancelText: false,
          callback: (result) => {
            if (result.confirmed) {
              this.promptNotificationPermission();
            }
          }
        })
      }
    });
  }
  ngOnInit(): void {

    if (typeof Worker !== 'undefined') {
      // Create a new
      this.showNotificationPermission.set(Notification.permission === "default");
      if (Notification.permission === "granted") {
        this.startWebworker();
      }
    }
  }

  promptNotificationPermission() {
    const startWebworker = this.startWebworker.bind(this);
    Notification.requestPermission(function(permission) {
      // If the user accepts, let's create a notification
      if (permission === "granted") {
        startWebworker();
      }
    });
  }

  startWebworker() {
    const authState = this.authService.getAuthState()
    const worker = new Worker(new URL('../../../app.worker', import.meta.url));
    const url = environment.apiBaseUrl
    authState.subscribe(({ token }) => {
      worker.postMessage({ url, token, location: window.location.origin })
    })

  }
}
