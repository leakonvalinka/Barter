import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { environment } from '../environments/environment';
import { AuthenticationService } from './services/auth/auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: `<router-outlet></router-outlet>`
})
export class AppComponent implements OnInit {
  constructor(private authService: AuthenticationService) {

  }
  ngOnInit(): void {
    // if (typeof Worker !== 'undefined') {
    //   // Create a new
    //   const authState = this.authService.getAuthState()
    //   Notification.requestPermission(function(permission) {
    //     // If the user accepts, let's create a notification
    //     if (permission === "granted") {
    //       // worker.port.postMessage({ name: "notification" });
    //       const worker = new Worker(new URL('./app.worker', import.meta.url));
    //       // worker.postMessage({ url: environment.apiBaseUrl })
    //       const url = environment.apiBaseUrl
    //       authState.subscribe(({ token }) => {
    //         worker.postMessage({ url, token, location: window.location.origin })
    //       })
    //     }
    //   });
    // } else {
    //   // Web Workers are not supported in this environment.
    //   // You should add a fallback so that your program still executes correctly.
    // }
  }
  title = 'Barter';
}


